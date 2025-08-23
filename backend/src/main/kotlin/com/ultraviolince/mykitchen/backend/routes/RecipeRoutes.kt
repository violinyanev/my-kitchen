package com.ultraviolince.mykitchen.backend.routes

import com.ultraviolince.mykitchen.backend.auth.AuthenticationService
import com.ultraviolince.mykitchen.backend.database.RecipeDatabase
import com.ultraviolince.mykitchen.backend.model.ErrorResponse
import com.ultraviolince.mykitchen.backend.model.RecipeRequest
import com.ultraviolince.mykitchen.backend.model.RecipeResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing

fun Application.configureRecipeRoutes(
    recipeDatabase: RecipeDatabase,
    authService: AuthenticationService
) {
    routing {
        get("/recipes") {
            val token = call.request.headers["Authorization"]?.removePrefix("Bearer ")
            val user = authService.authenticate(token)
            
            if (user == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse(
                        message = "Authentication Token is missing!",
                        data = null,
                        error = "Unauthorized"
                    )
                )
                return@get
            }
            
            val all = call.request.queryParameters["all"]?.toBooleanStrictOrNull() ?: false
            val recipes = recipeDatabase.get(user, all)
            call.respond(HttpStatusCode.OK, recipes)
        }

        post("/recipes") {
            val token = call.request.headers["Authorization"]?.removePrefix("Bearer ")
            val user = authService.authenticate(token)
            
            if (user == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse(
                        message = "Authentication Token is missing!",
                        data = null,
                        error = "Unauthorized"
                    )
                )
                return@post
            }
            
            try {
                val recipeRequest = call.receive<RecipeRequest>()
                val (recipe, error) = recipeDatabase.put(user, recipeRequest)
                
                if (error != null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(message = error, error = "Bad Request")
                    )
                    return@post
                }
                
                if (recipe != null) {
                    val response = RecipeResponse(
                        message = "Recipe created successfully",
                        recipe = recipe
                    )
                    call.respond(HttpStatusCode.Created, response)
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(message = "Unknown error", error = "Bad Request")
                )
            }
        }

        delete("/recipes/{id}") {
            val token = call.request.headers["Authorization"]?.removePrefix("Bearer ")
            val user = authService.authenticate(token)
            
            if (user == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse(
                        message = "Authentication Token is missing!",
                        data = null,
                        error = "Unauthorized"
                    )
                )
                return@delete
            }
            
            val recipeIdParam = call.parameters["id"]
            if (recipeIdParam == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(message = "Recipe ID is required", error = "Bad Request")
                )
                return@delete
            }
            
            val recipeId = recipeIdParam.toIntOrNull()
            if (recipeId == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(message = "Invalid recipe ID", error = "Bad Request")
                )
                return@delete
            }
            
            val (success, result) = recipeDatabase.delete(user, recipeId)
            if (success) {
                val response = mapOf(
                    "message" to "Recipe deleted successfully",
                    "recipe" to result
                )
                call.respond(HttpStatusCode.NoContent, response)
            } else {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(message = result.toString(), error = "Bad Request")
                )
            }
        }
    }
}