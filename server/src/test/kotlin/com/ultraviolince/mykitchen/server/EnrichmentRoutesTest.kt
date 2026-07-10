package com.ultraviolince.mykitchen.server

import com.ultraviolince.mykitchen.server.config.AppConfig
import com.ultraviolince.mykitchen.server.data.repository.UserRepository
import com.ultraviolince.mykitchen.server.data.tables.RecipeEnrichments
import com.ultraviolince.mykitchen.server.data.tables.Recipes
import com.ultraviolince.mykitchen.server.plugins.configureAuthentication
import com.ultraviolince.mykitchen.server.plugins.configureSerialization
import com.ultraviolince.mykitchen.server.plugins.configureStatusPages
import com.ultraviolince.mykitchen.server.plugins.configureTestDatabase
import com.ultraviolince.mykitchen.server.plugins.dropTestDatabase
import com.ultraviolince.mykitchen.server.routes.authRoutes
import com.ultraviolince.mykitchen.server.routes.enrichmentRoutes
import com.ultraviolince.mykitchen.server.routes.recipeRoutes
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

/** Client + auth token + a pre-created recipe, ready for enrichment calls. */
private class Ctx(val client: HttpClient, val token: String, val recipeId: String)

class EnrichmentRoutesTest {

    private fun testApp(block: suspend Ctx.() -> Unit) {
        val config = AppConfig(
            jwtSecret = "test-secret",
            jwtIssuer = "test-issuer",
            jwtAudience = "test-audience",
            databaseUrl = "jdbc:h2:mem:test_${System.nanoTime()};DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
            databaseUser = "sa",
            databasePassword = "",
            databaseDriver = "org.h2.Driver",
            corsAllowedOrigins = null,
            ollamaBaseUrl = "http://unused",
            ollamaModel = "gemma4:26b",
            unsplashAccessKey = null,
        )
        configureTestDatabase(config.databaseUrl)
        UserRepository.create("user@test.com", "password123")
        try {
            testApplication {
                application {
                    configureSerialization()
                    configureAuthentication(config)
                    configureStatusPages()
                    routing {
                        authRoutes(config)
                        recipeRoutes()
                        enrichmentRoutes()
                    }
                }
                val client = createClient {
                    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
                }
                val token = client.post("/users/login") {
                    contentType(ContentType.Application.Json)
                    setBody(LoginBody("user@test.com", "password123"))
                }.body<JsonObject>()["token"]!!.jsonPrimitive.content
                val recipeId = client.post("/recipes") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody(RecipeBody("Pasta", "Boil pasta, add sauce"))
                }.body<JsonObject>()["id"]!!.jsonPrimitive.content
                Ctx(client, token, recipeId).block()
            }
        } finally {
            dropTestDatabase()
        }
    }

    /** Simulates the AutoBeautifyWorker having enriched the recipe. */
    private fun seedEnrichment(recipeId: String, summary: String = "A tasty dish") {
        transaction {
            val userId = Recipes.selectAll()
                .where { Recipes.id eq UUID.fromString(recipeId) }
                .first()[Recipes.userId].value
            val now = System.currentTimeMillis()
            RecipeEnrichments.insert { row ->
                row[RecipeEnrichments.recipeId] = UUID.fromString(recipeId)
                row[RecipeEnrichments.userId] = userId
                row[RecipeEnrichments.imageUrl] = null
                row[RecipeEnrichments.imageCredit] = null
                row[RecipeEnrichments.links] = """[{"title":"Recipe","url":"http://example.com","description":"desc"}]"""
                row[RecipeEnrichments.tags] = """["quick"]"""
                row[RecipeEnrichments.summary] = summary
                row[RecipeEnrichments.conversationHistory] = "[]"
                row[RecipeEnrichments.createdAt] = now
                row[RecipeEnrichments.updatedAt] = now
            }
        }
    }

    @Test
    fun getEnrichmentRequiresAuth() = testApp {
        val response = client.get("/recipes/$recipeId/enrichment")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun getEnrichmentReturns404WhenNoneExists() = testApp {
        val response = client.get("/recipes/$recipeId/enrichment") {
            bearerAuth(token)
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun getEnrichmentReturnsStoredEnrichment() = testApp {
        seedEnrichment(recipeId)
        val response = client.get("/recipes/$recipeId/enrichment") { bearerAuth(token) }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<JsonObject>()
        assertEquals("A tasty dish", body["summary"]!!.jsonPrimitive.content)
    }

    @Test
    fun getEnrichmentReturns404ForAnotherUsersRecipe() = testApp {
        seedEnrichment(recipeId)
        UserRepository.create("other@test.com", "password123")
        val otherToken = client.post("/users/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginBody("other@test.com", "password123"))
        }.body<JsonObject>()["token"]!!.jsonPrimitive.content
        val response = client.get("/recipes/$recipeId/enrichment") { bearerAuth(otherToken) }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun deletingRecipeAlsoRemovesItsEnrichment() = testApp {
        seedEnrichment(recipeId)
        val response = client.delete("/recipes/$recipeId") { bearerAuth(token) }
        assertEquals(HttpStatusCode.NoContent, response.status)
        val remaining = transaction {
            RecipeEnrichments.selectAll()
                .where { RecipeEnrichments.recipeId eq UUID.fromString(recipeId) }
                .count()
        }
        assertEquals(0, remaining)
    }
}
