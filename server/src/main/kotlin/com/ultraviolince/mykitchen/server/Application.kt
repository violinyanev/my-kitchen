package com.ultraviolince.mykitchen.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resources
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.Principal
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTAuthenticationProvider
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class LoginResponse(val token: String)

@Serializable
data class RecipeDto(
    val id: Long,
    val title: String,
    val ingredients: List<String>,
    val content: String,
    val timestamp: Long
)

data class UserPrincipal(val username: String) : Principal

fun main() {
    embeddedServer(Netty, port = 5000) { module() }.start(wait = true)
}

fun Application.module() {
    install(Resources)
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    install(WebSockets)

    val secret = System.getenv("RECIPES_SECRET_KEY") ?: "dev-secret"
    val algorithm = Algorithm.HMAC256(secret)

    install(Authentication) {
        jwt {
            verifier(JWT.require(algorithm).build())
            validate { cred -> cred.payload.getClaim("username").asString()?.let { UserPrincipal(it) } }
        }
    }

    val store = InMemoryStore()

    routing {
        get("/health") { call.respondText("OK", ContentType.Text.Plain) }

        post("/users/login") {
            val req = call.receive<LoginRequest>()
            if (req.email.isBlank() || req.password.isBlank()) {
                call.respond(HttpStatusCode.BadRequest)
            } else {
                val token = JWT.create().withClaim("username", req.email).sign(algorithm)
                call.respond(LoginResponse(token))
            }
        }

        authentication {
            jwt {}
        }

        get("/recipes") {
            val principal = call.authentication.principal<UserPrincipal>() ?: return@get call.respond(HttpStatusCode.Unauthorized)
            call.respond(store.getAll(principal.username))
        }

        post("/recipes") {
            val principal = call.authentication.principal<UserPrincipal>() ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val body = call.receive<RecipeDto>()
            store.upsert(principal.username, body)
            call.respond(HttpStatusCode.Created, mapOf("recipe" to body))
        }

        delete("/recipes/{id}") {
            val principal = call.authentication.principal<UserPrincipal>() ?: return@delete call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"]?.toLongOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest)
            store.delete(principal.username, id)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}

class InMemoryStore {
    private val userToRecipes = mutableMapOf<String, MutableMap<Long, RecipeDto>>()

    fun getAll(user: String): List<RecipeDto> = userToRecipes[user]?.values?.sortedBy { it.id } ?: emptyList()

    fun upsert(user: String, recipe: RecipeDto) {
        val userStore = userToRecipes.getOrPut(user) { mutableMapOf() }
        userStore[recipe.id] = recipe
    }

    fun delete(user: String, id: Long) {
        userToRecipes[user]?.remove(id)
    }
}

