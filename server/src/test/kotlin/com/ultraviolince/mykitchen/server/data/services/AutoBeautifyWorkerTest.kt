package com.ultraviolince.mykitchen.server.data.services

import com.ultraviolince.mykitchen.server.config.AppConfig
import com.ultraviolince.mykitchen.server.data.repository.UserRepository
import com.ultraviolince.mykitchen.server.data.tables.RecipeEnrichments
import com.ultraviolince.mykitchen.server.data.tables.Recipes
import com.ultraviolince.mykitchen.server.plugins.configureTestDatabase
import com.ultraviolince.mykitchen.server.plugins.dropTestDatabase
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AutoBeautifyWorkerTest {

    private lateinit var userId: UUID

    private val llmResponse = """
        {"summary":"Auto-beautified","tags":["quick"],
         "links":[{"title":"Recipe","url":"http://example.com","description":"desc"}],
         "imageSearchQuery":""}
    """.trimIndent()

    @BeforeTest
    fun setUp() {
        configureTestDatabase("jdbc:h2:mem:worker_${System.nanoTime()};DB_CLOSE_DELAY=-1;MODE=PostgreSQL")
        userId = UserRepository.create("worker@test.com", "password123")
    }

    @AfterTest
    fun tearDown() {
        dropTestDatabase()
    }

    private fun configFor(baseUrl: String) = AppConfig(
        jwtSecret = "test-secret",
        jwtIssuer = "test-issuer",
        jwtAudience = "test-audience",
        databaseUrl = "jdbc:h2:mem:unused",
        databaseUser = "sa",
        databasePassword = "",
        databaseDriver = "org.h2.Driver",
        corsAllowedOrigins = null,
        ollamaBaseUrl = baseUrl,
        ollamaModel = "gemma4:26b",
        unsplashAccessKey = null,
    )

    private fun insertRecipe(title: String, createdAt: Long = System.currentTimeMillis()): UUID = transaction {
        Recipes.insert { row ->
            row[Recipes.userId] = this@AutoBeautifyWorkerTest.userId
            row[Recipes.title] = title
            row[Recipes.content] = "Some instructions"
            row[Recipes.createdAt] = createdAt
            row[Recipes.updatedAt] = createdAt
        }.resultedValues!!.first()[Recipes.id].value
    }

    private fun enrichmentSummaryFor(recipeId: UUID): String? = transaction {
        RecipeEnrichments.selectAll()
            .where { RecipeEnrichments.recipeId eq recipeId }
            .firstOrNull()
            ?.get(RecipeEnrichments.summary)
    }

    @Test
    fun processNextReturnsFalseWhenNoRecipesExist() = runBlocking {
        FakeLlmServer.respondingWith(llmResponse).use { fake ->
            val worker = AutoBeautifyWorker(EnrichmentService(configFor(fake.baseUrl)))
            assertFalse(worker.processNext())
        }
    }

    @Test
    fun processNextEnrichesARecipeWithoutEnrichment() = runBlocking {
        val recipeId = insertRecipe("Pasta")
        FakeLlmServer.respondingWith(llmResponse).use { fake ->
            val worker = AutoBeautifyWorker(EnrichmentService(configFor(fake.baseUrl)))
            assertTrue(worker.processNext())
            assertEquals("Auto-beautified", enrichmentSummaryFor(recipeId))
        }
    }

    @Test
    fun processNextReturnsFalseWhenAllRecipesAreEnriched() = runBlocking {
        insertRecipe("Pasta")
        FakeLlmServer.respondingWith(llmResponse).use { fake ->
            val worker = AutoBeautifyWorker(EnrichmentService(configFor(fake.baseUrl)))
            assertTrue(worker.processNext())
            assertFalse(worker.processNext())
        }
    }

    @Test
    fun processNextHandlesRecipesOldestFirst() = runBlocking {
        val older = insertRecipe("Older", createdAt = 1000)
        val newer = insertRecipe("Newer", createdAt = 2000)
        FakeLlmServer.respondingWith(llmResponse).use { fake ->
            val worker = AutoBeautifyWorker(EnrichmentService(configFor(fake.baseUrl)))
            assertTrue(worker.processNext())
            assertEquals("Auto-beautified", enrichmentSummaryFor(older))
            assertEquals(null, enrichmentSummaryFor(newer))
        }
    }

    @Test
    fun processNextReenrichesRecipeEditedAfterEnrichment() = runBlocking {
        val recipeId = insertRecipe("Pasta")
        FakeLlmServer.respondingWith(llmResponse).use { fake ->
            val worker = AutoBeautifyWorker(EnrichmentService(configFor(fake.baseUrl)))
            assertTrue(worker.processNext())
        }
        // Edit the recipe after enrichment: it becomes stale and is redone.
        transaction {
            Recipes.update(where = { Recipes.id eq recipeId }) { row ->
                row[Recipes.updatedAt] = System.currentTimeMillis() + 60_000
            }
        }
        val updatedResponse = llmResponse.replace("Auto-beautified", "Re-beautified")
        FakeLlmServer.respondingWith(updatedResponse).use { fake ->
            val worker = AutoBeautifyWorker(EnrichmentService(configFor(fake.baseUrl)))
            assertTrue(worker.processNext())
            assertEquals("Re-beautified", enrichmentSummaryFor(recipeId))
        }
        // Only one enrichment row exists — the stale one was updated in place.
        val rows = transaction {
            RecipeEnrichments.selectAll().where { RecipeEnrichments.recipeId eq recipeId }.count()
        }
        assertEquals(1, rows)
    }

    @Test
    fun processNextPropagatesLlmFailureWithoutStoringAnything() {
        val recipeId = insertRecipe("Pasta")
        val service = EnrichmentService(configFor(FakeLlmServer.unreachableUrl()))
        val worker = AutoBeautifyWorker(service)
        assertFailsWith<EnrichmentService.EnrichmentUnavailableException> {
            runBlocking { worker.processNext() }
        }
        assertEquals(null, enrichmentSummaryFor(recipeId))
    }
}
