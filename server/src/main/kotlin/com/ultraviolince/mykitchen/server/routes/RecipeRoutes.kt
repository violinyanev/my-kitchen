package com.ultraviolince.mykitchen.server.routes

import com.ultraviolince.mykitchen.server.data.dto.CreateRecipeRequestDto
import com.ultraviolince.mykitchen.server.data.dto.ErrorDto
import com.ultraviolince.mykitchen.server.data.dto.RecipeResponseDto
import com.ultraviolince.mykitchen.server.data.tables.Recipes
import com.ultraviolince.mykitchen.server.plugins.JWT_AUTH_NAME
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.util.UUID

fun Route.recipeRoutes() {
    authenticate(JWT_AUTH_NAME) {
        route("/recipes") {
            get { call.handleGetRecipes() }
            post { call.handleCreateRecipe() }
            put("/{id}") { call.handleUpdateRecipe() }
            delete("/{id}") { call.handleDeleteRecipe() }
        }
    }
}

private suspend fun ApplicationCall.handleGetRecipes() {
    val userId = userIdOrUnauthorized() ?: return
    val recipes = transaction {
        Recipes.selectAll()
            .where { Recipes.userId eq UUID.fromString(userId) }
            .map { row ->
                RecipeResponseDto(
                    id = row[Recipes.id].value.toString(),
                    title = row[Recipes.title],
                    content = row[Recipes.content],
                    createdAt = row[Recipes.createdAt],
                    updatedAt = row[Recipes.updatedAt],
                )
            }
    }
    respond(recipes)
}

private suspend fun ApplicationCall.handleCreateRecipe() {
    val userId = userIdOrUnauthorized() ?: return
    val request = receive<CreateRecipeRequestDto>()
    if (request.title.isBlank()) {
        respond(HttpStatusCode.BadRequest, ErrorDto("Title is required"))
        return
    }
    val created = transaction {
        val now = System.currentTimeMillis()
        val newId = Recipes.insert { row ->
            row[Recipes.userId] = UUID.fromString(userId)
            row[Recipes.title] = request.title
            row[Recipes.content] = request.content
            row[Recipes.createdAt] = now
            row[Recipes.updatedAt] = now
        }.resultedValues!!.first()
        RecipeResponseDto(
            id = newId[Recipes.id].value.toString(),
            title = newId[Recipes.title],
            content = newId[Recipes.content],
            createdAt = newId[Recipes.createdAt],
            updatedAt = newId[Recipes.updatedAt],
        )
    }
    respond(HttpStatusCode.Created, created)
}

private suspend fun ApplicationCall.handleUpdateRecipe() {
    val userId = userIdOrUnauthorized() ?: return
    val recipeId = parameters["id"]
        ?: return respond(HttpStatusCode.BadRequest, ErrorDto("Recipe ID required"))
    val request = receive<CreateRecipeRequestDto>()
    val updated = transaction {
        val rows = Recipes.update(
            where = {
                (Recipes.id eq UUID.fromString(recipeId)) and
                    (Recipes.userId eq UUID.fromString(userId))
            },
        ) { row ->
            row[Recipes.title] = request.title
            row[Recipes.content] = request.content
            row[Recipes.updatedAt] = System.currentTimeMillis()
        }
        if (rows == 0) return@transaction null
        Recipes.selectAll()
            .where { Recipes.id eq UUID.fromString(recipeId) }
            .firstOrNull()
            ?.let { row ->
                RecipeResponseDto(
                    id = row[Recipes.id].value.toString(),
                    title = row[Recipes.title],
                    content = row[Recipes.content],
                    createdAt = row[Recipes.createdAt],
                    updatedAt = row[Recipes.updatedAt],
                )
            }
    }
    if (updated == null) {
        respond(HttpStatusCode.NotFound, ErrorDto("Recipe not found"))
    } else {
        respond(updated)
    }
}

private suspend fun ApplicationCall.handleDeleteRecipe() {
    val userId = userIdOrUnauthorized() ?: return
    val recipeId = parameters["id"]
        ?: return respond(HttpStatusCode.BadRequest, ErrorDto("Recipe ID required"))
    val deleted = transaction {
        Recipes.deleteWhere {
            (Recipes.id eq UUID.fromString(recipeId)) and
                (Recipes.userId eq UUID.fromString(userId))
        }
    }
    if (deleted == 0) {
        respond(HttpStatusCode.NotFound, ErrorDto("Recipe not found"))
    } else {
        respond(HttpStatusCode.NoContent, "")
    }
}

private suspend fun ApplicationCall.userIdOrUnauthorized(): String? {
    val principal = principal<JWTPrincipal>()
    val userId = principal?.subject
    return if (userId == null) {
        respond(HttpStatusCode.Unauthorized, ErrorDto("Unauthorized"))
        null
    } else {
        userId
    }
}
