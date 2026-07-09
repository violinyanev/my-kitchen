package com.ultraviolince.mykitchen.server

import com.ultraviolince.mykitchen.server.config.AppConfig
import com.ultraviolince.mykitchen.server.data.repository.UserRepository
import com.ultraviolince.mykitchen.server.data.services.EnrichmentService
import com.ultraviolince.mykitchen.server.data.services.FakeLlmServer
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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@Serializable
private data class RefineBody(val feedback: String)

/** Client + auth token + a pre-created recipe, ready for enrichment calls. */
private class Ctx(val client: HttpClient, val token: String, val recipeId: String)

class EnrichmentRoutesTest {

    private lateinit var fakeLlm: FakeLlmServer

    private val llmResponse = """
        {"summary":"A tasty dish","tags":["quick"],
         "links":[{"title":"Recipe","url":"http://example.com","description":"desc"}],
         "imageSearchQuery":""}
    """.trimIndent()

    @BeforeTest
    fun setUp() {
        fakeLlm = FakeLlmServer.respondingWith(llmResponse)
    }

    @AfterTest
    fun tearDown() {
        fakeLlm.close()
    }

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
            ollamaBaseUrl = fakeLlm.baseUrl,
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
                        enrichmentRoutes(EnrichmentService(config))
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

    @Test
    fun beautifyRequiresAuth() = testApp {
        val response = client.post("/recipes/$recipeId/enrichment/beautify")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun beautifyReturns404ForUnknownRecipe() = testApp {
        val response = client.post("/recipes/00000000-0000-0000-0000-000000000000/enrichment/beautify") {
            bearerAuth(token)
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun beautifyCreatesEnrichmentOnSuccess() = testApp {
        val response = client.post("/recipes/$recipeId/enrichment/beautify") {
            bearerAuth(token)
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<JsonObject>()
        assertEquals("A tasty dish", body["summary"]!!.jsonPrimitive.content)
    }

    @Test
    fun beautifyReturns503WhenLlmUnavailable() {
        fakeLlm.close()
        fakeLlm = FakeLlmServer.respondingWithStatus(500)
        testApp {
            val response = client.post("/recipes/$recipeId/enrichment/beautify") {
                bearerAuth(token)
            }
            assertEquals(HttpStatusCode.ServiceUnavailable, response.status)
        }
    }

    @Test
    fun getEnrichmentReturns404WhenNoneExists() = testApp {
        val response = client.get("/recipes/$recipeId/enrichment") {
            bearerAuth(token)
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun getEnrichmentReturnsStoredEnrichmentAfterBeautify() = testApp {
        client.post("/recipes/$recipeId/enrichment/beautify") { bearerAuth(token) }
        val response = client.get("/recipes/$recipeId/enrichment") { bearerAuth(token) }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<JsonObject>()
        assertEquals("A tasty dish", body["summary"]!!.jsonPrimitive.content)
    }

    @Test
    fun refineRequiresExistingEnrichment() = testApp {
        val response = client.post("/recipes/$recipeId/enrichment/refine") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(RefineBody("spicier please"))
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun refineRejectsBlankFeedback() = testApp {
        client.post("/recipes/$recipeId/enrichment/beautify") { bearerAuth(token) }
        val response = client.post("/recipes/$recipeId/enrichment/refine") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(RefineBody("   "))
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun refineUpdatesExistingEnrichment() = testApp {
        client.post("/recipes/$recipeId/enrichment/beautify") { bearerAuth(token) }
        val response = client.post("/recipes/$recipeId/enrichment/refine") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(RefineBody("spicier please"))
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun deleteEnrichmentReturns404WhenNoneExists() = testApp {
        val response = client.delete("/recipes/$recipeId/enrichment") { bearerAuth(token) }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun deleteEnrichmentRemovesExistingOne() = testApp {
        client.post("/recipes/$recipeId/enrichment/beautify") { bearerAuth(token) }
        val response = client.delete("/recipes/$recipeId/enrichment") { bearerAuth(token) }
        assertEquals(HttpStatusCode.NoContent, response.status)
        val getAfter = client.get("/recipes/$recipeId/enrichment") { bearerAuth(token) }
        assertEquals(HttpStatusCode.NotFound, getAfter.status)
    }
}
