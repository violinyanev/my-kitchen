package com.ultraviolince.mykitchen.server.routes

import com.ultraviolince.mykitchen.server.data.dto.EnrichmentResponseDto
import com.ultraviolince.mykitchen.server.data.dto.ErrorDto
import com.ultraviolince.mykitchen.server.data.dto.RecipeLinkDto
import com.ultraviolince.mykitchen.server.data.dto.RefineEnrichmentRequestDto
import com.ultraviolince.mykitchen.server.data.services.EnrichmentService
import com.ultraviolince.mykitchen.server.data.services.EnrichmentService.EnrichmentUnavailableException
import com.ultraviolince.mykitchen.server.data.tables.RecipeEnrichments
import com.ultraviolince.mykitchen.server.data.tables.Recipes
import com.ultraviolince.mykitchen.server.plugins.JWT_AUTH_NAME
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.util.UUID

fun Route.enrichmentRoutes(service: EnrichmentService) {
    authenticate(JWT_AUTH_NAME) {
        route("/recipes/{recipeId}/enrichment") {
            post("/beautify") { call.handleBeautify(service) }
            get { call.handleGetEnrichment() }
            post("/refine") { call.handleRefine(service) }
            delete { call.handleDeleteEnrichment() }
        }
    }
}

private suspend fun io.ktor.server.application.ApplicationCall.handleBeautify(
    service: EnrichmentService,
) {
    val userId = userIdOrUnauthorized() ?: return
    val recipeId = parameters["recipeId"]
        ?: return respond(HttpStatusCode.BadRequest, ErrorDto("Recipe ID required"))

    val recipe = transaction {
        Recipes.selectAll()
            .where { (Recipes.id eq UUID.fromString(recipeId)) and (Recipes.userId eq UUID.fromString(userId)) }
            .firstOrNull()
    } ?: return respond(HttpStatusCode.NotFound, ErrorDto("Recipe not found"))

    val result = try {
        service.enrich(recipe[Recipes.title], recipe[Recipes.content])
    } catch (_: EnrichmentUnavailableException) {
        return respond(HttpStatusCode.ServiceUnavailable, ErrorDto("Beautify service is temporarily unavailable"))
    }

    val dto = transaction {
        transaction {
            RecipeEnrichments.deleteWhere {
                (RecipeEnrichments.recipeId eq UUID.fromString(recipeId)) and
                    (RecipeEnrichments.userId eq UUID.fromString(userId))
            }
        }
        val now = System.currentTimeMillis()
        val inserted = RecipeEnrichments.insert { row ->
            row[RecipeEnrichments.recipeId] = UUID.fromString(recipeId)
            row[RecipeEnrichments.userId] = UUID.fromString(userId)
            row[RecipeEnrichments.imageUrl] = result.imageUrl
            row[RecipeEnrichments.imageCredit] = result.imageCredit
            row[RecipeEnrichments.links] = Json.encodeToString(result.links)
            row[RecipeEnrichments.tags] = Json.encodeToString(result.tags)
            row[RecipeEnrichments.summary] = result.summary
            row[RecipeEnrichments.conversationHistory] = result.conversationHistory
            row[RecipeEnrichments.createdAt] = now
            row[RecipeEnrichments.updatedAt] = now
        }.resultedValues!!.first()
        inserted.toEnrichmentDto()
    }

    respond(dto)
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

private suspend fun io.ktor.server.application.ApplicationCall.handleRefine(
    service: EnrichmentService,
) {
    val userId = userIdOrUnauthorized() ?: return
    val recipeId = parameters["recipeId"]
        ?: return respond(HttpStatusCode.BadRequest, ErrorDto("Recipe ID required"))

    val request = receive<RefineEnrichmentRequestDto>()
    if (request.feedback.isBlank()) {
        respond(HttpStatusCode.BadRequest, ErrorDto("Feedback is required"))
        return
    }

    val existing = transaction {
        RecipeEnrichments.selectAll()
            .where {
                (RecipeEnrichments.recipeId eq UUID.fromString(recipeId)) and
                    (RecipeEnrichments.userId eq UUID.fromString(userId))
            }
            .firstOrNull()
    } ?: return respond(HttpStatusCode.NotFound, ErrorDto("No enrichment to refine — run /beautify first"))

    val conversationHistory = existing[RecipeEnrichments.conversationHistory]
    val result = try {
        service.refine(request.feedback, conversationHistory)
    } catch (_: EnrichmentUnavailableException) {
        return respond(HttpStatusCode.ServiceUnavailable, ErrorDto("Beautify service is temporarily unavailable"))
    }

    val dto = transaction {
        RecipeEnrichments.update(
            where = {
                (RecipeEnrichments.recipeId eq UUID.fromString(recipeId)) and
                    (RecipeEnrichments.userId eq UUID.fromString(userId))
            },
        ) { row ->
            row[RecipeEnrichments.imageUrl] = result.imageUrl
            row[RecipeEnrichments.imageCredit] = result.imageCredit
            row[RecipeEnrichments.links] = Json.encodeToString(result.links)
            row[RecipeEnrichments.tags] = Json.encodeToString(result.tags)
            row[RecipeEnrichments.summary] = result.summary
            row[RecipeEnrichments.conversationHistory] = result.conversationHistory
            row[RecipeEnrichments.updatedAt] = System.currentTimeMillis()
        }
        RecipeEnrichments.selectAll()
            .where {
                (RecipeEnrichments.recipeId eq UUID.fromString(recipeId)) and
                    (RecipeEnrichments.userId eq UUID.fromString(userId))
            }
            .first()
            .toEnrichmentDto()
    }

    respond(dto)
}

private suspend fun io.ktor.server.application.ApplicationCall.handleDeleteEnrichment() {
    val userId = userIdOrUnauthorized() ?: return
    val recipeId = parameters["recipeId"]
        ?: return respond(HttpStatusCode.BadRequest, ErrorDto("Recipe ID required"))

    val deleted = transaction {
        RecipeEnrichments.deleteWhere {
            (RecipeEnrichments.recipeId eq UUID.fromString(recipeId)) and
                (RecipeEnrichments.userId eq UUID.fromString(userId))
        }
    }

    if (deleted == 0) {
        respond(HttpStatusCode.NotFound, ErrorDto("No enrichment found"))
    } else {
        respond(HttpStatusCode.NoContent, "")
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
