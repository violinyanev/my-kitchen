package com.ultraviolince.mykitchen.server.data.services

import com.ultraviolince.mykitchen.server.data.tables.RecipeEnrichments
import com.ultraviolince.mykitchen.server.data.tables.Recipes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.core.leftJoin
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.slf4j.LoggerFactory
import java.util.UUID

/**
 * Background worker that beautifies recipes automatically, one at a time.
 *
 * Each pass picks the oldest recipe that has no enrichment yet — or whose
 * enrichment is older than the recipe's last edit — enriches it via the LLM,
 * and stores the result. The LLM serializes generations anyway, so processing
 * sequentially also keeps the queue fair.
 */
class AutoBeautifyWorker(
    private val service: EnrichmentService,
    private val idleDelayMillis: Long = IDLE_DELAY_MILLIS,
    private val errorDelayMillis: Long = ERROR_DELAY_MILLIS,
) {

    data class Candidate(
        val recipeId: UUID,
        val userId: UUID,
        val title: String,
        val content: String,
    )

    /** Runs forever: beautifies pending recipes, then polls for new work. */
    suspend fun run() {
        logger.info("Auto-beautify worker started")
        while (currentCoroutineContext().isActive) {
            val didWork = try {
                processNext()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.warn("Auto-beautify pass failed, retrying in ${errorDelayMillis / 1000}s: ${e.message}")
                delay(errorDelayMillis)
                continue
            }
            if (!didWork) delay(idleDelayMillis)
        }
    }

    /**
     * Beautifies the next recipe that needs it.
     * Returns false when no recipe is pending.
     */
    suspend fun processNext(): Boolean {
        val candidate = transaction { findCandidate() } ?: return false
        logger.info("Beautifying recipe ${candidate.recipeId} (\"${candidate.title}\")")
        val result = service.enrich(candidate.title, candidate.content)
        transaction { storeEnrichment(candidate, result) }
        return true
    }

    private fun findCandidate(): Candidate? =
        Recipes.leftJoin(RecipeEnrichments)
            .selectAll()
            .where {
                RecipeEnrichments.id.isNull() or
                    (RecipeEnrichments.updatedAt less Recipes.updatedAt)
            }
            .orderBy(Recipes.createdAt)
            .limit(1)
            .firstOrNull()
            ?.let { row ->
                Candidate(
                    recipeId = row[Recipes.id].value,
                    userId = row[Recipes.userId].value,
                    title = row[Recipes.title],
                    content = row[Recipes.content],
                )
            }

    private fun storeEnrichment(candidate: Candidate, result: EnrichmentService.EnrichmentResult) {
        val now = System.currentTimeMillis()
        val updated = RecipeEnrichments.update(
            where = { RecipeEnrichments.recipeId eq candidate.recipeId },
        ) { row ->
            row[RecipeEnrichments.imageUrl] = result.imageUrl
            row[RecipeEnrichments.imageCredit] = result.imageCredit
            row[RecipeEnrichments.links] = Json.encodeToString(result.links)
            row[RecipeEnrichments.tags] = Json.encodeToString(result.tags)
            row[RecipeEnrichments.summary] = result.summary
            row[RecipeEnrichments.conversationHistory] = result.conversationHistory
            row[RecipeEnrichments.updatedAt] = now
        }
        if (updated == 0) {
            RecipeEnrichments.insert { row ->
                row[RecipeEnrichments.recipeId] = candidate.recipeId
                row[RecipeEnrichments.userId] = candidate.userId
                row[RecipeEnrichments.imageUrl] = result.imageUrl
                row[RecipeEnrichments.imageCredit] = result.imageCredit
                row[RecipeEnrichments.links] = Json.encodeToString(result.links)
                row[RecipeEnrichments.tags] = Json.encodeToString(result.tags)
                row[RecipeEnrichments.summary] = result.summary
                row[RecipeEnrichments.conversationHistory] = result.conversationHistory
                row[RecipeEnrichments.createdAt] = now
                row[RecipeEnrichments.updatedAt] = now
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AutoBeautifyWorker::class.java)

        /** How long to wait before checking for new recipes when idle. */
        private const val IDLE_DELAY_MILLIS = 60_000L

        /** Back-off after a failed pass (LLM down, DB hiccup). */
        private const val ERROR_DELAY_MILLIS = 300_000L
    }
}
