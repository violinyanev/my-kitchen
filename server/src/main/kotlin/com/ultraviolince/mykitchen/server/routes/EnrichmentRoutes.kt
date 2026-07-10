package com.ultraviolince.mykitchen.server.routes

import com.ultraviolince.mykitchen.server.data.dto.EnrichmentResponseDto
import com.ultraviolince.mykitchen.server.data.dto.ErrorDto
import com.ultraviolince.mykitchen.server.data.dto.RecipeLinkDto
import com.ultraviolince.mykitchen.server.data.tables.RecipeEnrichments
import com.ultraviolince.mykitchen.server.plugins.JWT_AUTH_NAME
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.UUID

/**
 * Read-only enrichment access. Enrichments are produced automatically by
 * [com.ultraviolince.mykitchen.server.data.services.AutoBeautifyWorker];
 * clients only fetch them to show the beautified version of a recipe.
 */
fun Route.enrichmentRoutes() {
    authenticate(JWT_AUTH_NAME) {
        route("/recipes/{recipeId}/enrichment") {
            get { call.handleGetEnrichment() }
        }
        // All enrichments of the current user in one call, so the recipe list
        // can offer tag filtering without one request per recipe.
        get("/enrichments") { call.handleGetAllEnrichments() }
    }
}

private suspend fun io.ktor.server.application.ApplicationCall.handleGetAllEnrichments() {
    val userId = userIdOrUnauthorized() ?: return
    val enrichments = transaction {
        RecipeEnrichments.selectAll()
            .where { RecipeEnrichments.userId eq UUID.fromString(userId) }
            .map { it.toEnrichmentDto() }
    }
    respond(enrichments)
}

private suspend fun io.ktor.server.application.ApplicationCall.handleGetEnrichment() {
    val userId = userIdOrUnauthorized() ?: return
    val recipeId = parameters["recipeId"]
        ?: return respond(HttpStatusCode.BadRequest, ErrorDto("Recipe ID required"))

    val enrichment = transaction {
        RecipeEnrichments.selectAll()
            .where {
                (RecipeEnrichments.recipeId eq UUID.fromString(recipeId)) and
                    (RecipeEnrichments.userId eq UUID.fromString(userId))
            }
            .firstOrNull()
            ?.toEnrichmentDto()
    }

    if (enrichment == null) {
        respond(HttpStatusCode.NotFound, ErrorDto("No enrichment found"))
    } else {
        respond(enrichment)
    }
}

private fun org.jetbrains.exposed.v1.core.ResultRow.toEnrichmentDto(): EnrichmentResponseDto {
    val links = try {
        Json.decodeFromString<List<RecipeLinkDto>>(this[RecipeEnrichments.links])
    } catch (_: Exception) {
        emptyList()
    }
    val tags = try {
        Json.decodeFromString<List<String>>(this[RecipeEnrichments.tags])
    } catch (_: Exception) {
        emptyList()
    }
    return EnrichmentResponseDto(
        id = this[RecipeEnrichments.id].value.toString(),
        recipeId = this[RecipeEnrichments.recipeId].value.toString(),
        imageUrl = this[RecipeEnrichments.imageUrl],
        imageCredit = this[RecipeEnrichments.imageCredit],
        tags = tags,
        links = links,
        summary = this[RecipeEnrichments.summary],
        updatedAt = this[RecipeEnrichments.updatedAt],
    )
}

private suspend fun io.ktor.server.application.ApplicationCall.userIdOrUnauthorized(): String? {
    val principal = principal<JWTPrincipal>()
    val userId = principal?.subject
    return if (userId == null) {
        respond(HttpStatusCode.Unauthorized, ErrorDto("Unauthorized"))
        null
    } else {
        userId
    }
}
