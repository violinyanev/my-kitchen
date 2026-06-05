package com.ultraviolince.mykitchen.server.routes

import com.ultraviolince.mykitchen.server.data.dto.CreateRecipeRequestDto
import com.ultraviolince.mykitchen.server.data.dto.ErrorDto
import com.ultraviolince.mykitchen.server.data.dto.RecipeResponseDto
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
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

fun Route.recipeRoutes() {
    authenticate(JWT_AUTH_NAME) {
        route("/recipes") {
            get {
                val userId = call.userIdOrUnauthorized() ?: return@get
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
                call.respond(recipes)
            }

            post {
                val userId = call.userIdOrUnauthorized() ?: return@post
                val request = call.receive<CreateRecipeRequestDto>()
                if (request.title.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorDto("Title is required"))
                    return@post
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
                call.respond(HttpStatusCode.Created, created)
            }

            put("/{id}") {
                val userId = call.userIdOrUnauthorized() ?: return@put
                val recipeId = call.parameters["id"]
                    ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorDto("Recipe ID required"))
                val request = call.receive<CreateRecipeRequestDto>()

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
                    call.respond(HttpStatusCode.NotFound, ErrorDto("Recipe not found"))
                } else {
                    call.respond(updated)
                }
            }

            delete("/{id}") {
                val userId = call.userIdOrUnauthorized() ?: return@delete
                val recipeId = call.parameters["id"]
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorDto("Recipe ID required"))

                val deleted = transaction {
                    Recipes.deleteWhere {
                        (Recipes.id eq UUID.fromString(recipeId)) and
                            (Recipes.userId eq UUID.fromString(userId))
                    }
                }
                if (deleted == 0) {
                    call.respond(HttpStatusCode.NotFound, ErrorDto("Recipe not found"))
                } else {
                    call.respond(HttpStatusCode.NoContent, "")
                }
            }
        }
    }
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
