package com.ultraviolince.mykitchen.server

import com.ultraviolince.mykitchen.server.config.AppConfig
import com.ultraviolince.mykitchen.server.data.repository.UserRepository
import com.ultraviolince.mykitchen.server.plugins.configureAuthentication
import com.ultraviolince.mykitchen.server.plugins.configureCors
import com.ultraviolince.mykitchen.server.plugins.configureSerialization
import com.ultraviolince.mykitchen.server.plugins.configureStatusPages
import com.ultraviolince.mykitchen.server.plugins.configureTestDatabase
import com.ultraviolince.mykitchen.server.plugins.dropTestDatabase
import com.ultraviolince.mykitchen.server.routes.authRoutes
import com.ultraviolince.mykitchen.server.routes.healthRoutes
import com.ultraviolince.mykitchen.server.routes.recipeRoutes
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
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
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@Serializable
data class LoginBody(val email: String, val password: String)

@Serializable
data class RegisterBody(val email: String, val password: String)

@Serializable
data class RecipeBody(val title: String, val content: String)

class ServerTest {

    private val testConfig = AppConfig(
        jwtSecret = "test-secret",
        jwtIssuer = "test-issuer",
        jwtAudience = "test-audience",
        databaseUrl = "jdbc:h2:mem:test_${System.nanoTime()};DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        databaseUser = "sa",
        databasePassword = "",
        databaseDriver = "org.h2.Driver",
        corsAllowedOrigins = null,
        ollamaBaseUrl = "http://localhost:11434",
        ollamaModel = "gemma4:26b",
        unsplashAccessKey = null,
        devMode = true,
    )

    @BeforeTest
    fun setUp() {
        configureTestDatabase(testConfig.databaseUrl)
        UserRepository.create("user@test.com", "password123")
    }

    @AfterTest
    fun tearDown() {
        dropTestDatabase()
    }

    private fun testApp(block: suspend io.ktor.server.testing.ApplicationTestBuilder.() -> Unit) =
        testApplication {
            application {
                configureSerialization()
                configureAuthentication(testConfig)
                configureStatusPages()
                routing {
                    healthRoutes()
                    authRoutes(testConfig)
                    recipeRoutes()
                }
            }
            val client = createClient {
                install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            }
            block()
        }

    private suspend fun io.ktor.server.testing.ApplicationTestBuilder.loginAndGetToken(): String {
        val client = createClient {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
        val response = client.post("/users/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginBody("user@test.com", "password123"))
        }
        val body = response.body<JsonObject>()
        return body["token"]!!.jsonPrimitive.content
    }

    @Test
    fun healthReturnsOk() = testApplication {
        application {
            configureSerialization()
            routing { healthRoutes() }
        }
        val response = client.get("/health")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("OK", response.body<String>())
    }

    @Test
    fun loginSucceedsWithValidCredentials() = testApplication {
        application {
            configureSerialization()
            configureAuthentication(testConfig)
            configureStatusPages()
            routing {
                authRoutes(testConfig)
            }
        }
        val client = createClient {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
        val response = client.post("/users/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginBody("user@test.com", "password123"))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<JsonObject>()
        assertNotNull(body["token"])
        assertTrue(body["token"]!!.jsonPrimitive.content.isNotBlank())
    }

    @Test
    fun registerCreatesUserAndReturnsToken() = testApplication {
        application {
            configureSerialization()
            configureAuthentication(testConfig)
            configureStatusPages()
            routing { authRoutes(testConfig) }
        }
        val client = createClient {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
        val response = client.post("/users/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterBody("newuser@example.com", "securepassword"))
        }
        assertEquals(HttpStatusCode.Created, response.status)
        val body = response.body<JsonObject>()
        assertNotNull(body["token"])
        assertTrue(body["token"]!!.jsonPrimitive.content.isNotBlank())
    }

    @Test
    fun registerRejectsDuplicateEmail() = testApplication {
        application {
            configureSerialization()
            configureAuthentication(testConfig)
            configureStatusPages()
            routing { authRoutes(testConfig) }
        }
        val client = createClient {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
        client.post("/users/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterBody("dup@example.com", "securepassword"))
        }
        val response = client.post("/users/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterBody("dup@example.com", "securepassword"))
        }
        assertEquals(HttpStatusCode.Conflict, response.status)
    }

    @Test
    fun registerRejectsInvalidEmail() = testApplication {
        application {
            configureSerialization()
            configureAuthentication(testConfig)
            configureStatusPages()
            routing { authRoutes(testConfig) }
        }
        val client = createClient {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
        val response = client.post("/users/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterBody("notanemail", "securepassword"))
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun registerRejectsShortPassword() = testApplication {
        application {
            configureSerialization()
            configureAuthentication(testConfig)
            configureStatusPages()
            routing { authRoutes(testConfig) }
        }
        val client = createClient {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
        val response = client.post("/users/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterBody("user@example.com", "short"))
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun loginFailsWithWrongPassword() = testApplication {
        application {
            configureSerialization()
            configureAuthentication(testConfig)
            configureStatusPages()
            routing {
                authRoutes(testConfig)
            }
        }
        val client = createClient {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
        val response = client.post("/users/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginBody("user@test.com", "wrongpassword"))
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun getRecipesRequiresAuth() = testApplication {
        application {
            configureSerialization()
            configureAuthentication(testConfig)
            configureStatusPages()
            routing {
                recipeRoutes()
            }
        }
        val response = client.get("/recipes")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun createAndGetRecipe() = testApplication {
        application {
            configureSerialization()
            configureAuthentication(testConfig)
            configureStatusPages()
            routing {
                authRoutes(testConfig)
                recipeRoutes()
            }
        }
        val client = createClient {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val loginResponse = client.post("/users/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginBody("user@test.com", "password123"))
        }
        val token = loginResponse.body<JsonObject>()["token"]!!.jsonPrimitive.content

        val createResponse = client.post("/recipes") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(RecipeBody("Pasta", "Boil water and cook pasta"))
        }
        assertEquals(HttpStatusCode.Created, createResponse.status)
        val created = createResponse.body<JsonObject>()
        assertEquals("Pasta", created["title"]!!.jsonPrimitive.content)

        val listResponse = client.get("/recipes") {
            bearerAuth(token)
        }
        assertEquals(HttpStatusCode.OK, listResponse.status)
        val list = listResponse.body<List<JsonObject>>()
        assertEquals(1, list.size)
        assertEquals("Pasta", list.first()["title"]!!.jsonPrimitive.content)
    }

    @Test
    fun deleteRecipeRemovesIt() = testApplication {
        application {
            configureSerialization()
            configureAuthentication(testConfig)
            configureStatusPages()
            routing {
                authRoutes(testConfig)
                recipeRoutes()
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
            setBody(RecipeBody("ToDelete", "content"))
        }.body<JsonObject>()["id"]!!.jsonPrimitive.content

        val deleteResponse = client.delete("/recipes/$recipeId") { bearerAuth(token) }
        assertEquals(HttpStatusCode.NoContent, deleteResponse.status)

        val listAfter = client.get("/recipes") { bearerAuth(token) }.body<List<JsonObject>>()
        assertTrue(listAfter.isEmpty())
    }

    @Test
    fun loginWithInvalidEmailReturns400() = testApplication {
        application {
            configureSerialization()
            configureAuthentication(testConfig)
            configureStatusPages()
            routing { authRoutes(testConfig) }
        }
        val client = createClient { install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) } }
        val response = client.post("/users/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginBody("not-an-email", "password123"))
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun loginWithShortPasswordReturns400() = testApplication {
        application {
            configureSerialization()
            configureAuthentication(testConfig)
            configureStatusPages()
            routing { authRoutes(testConfig) }
        }
        val client = createClient { install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) } }
        val response = client.post("/users/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginBody("user@test.com", "short"))
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun loginWithNonExistentUserReturns401() = testApplication {
        application {
            configureSerialization()
            configureAuthentication(testConfig)
            configureStatusPages()
            routing { authRoutes(testConfig) }
        }
        val client = createClient { install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) } }
        val response = client.post("/users/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginBody("nobody@example.com", "password123"))
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun createRecipeWithBlankTitleReturns400() = testApplication {
        application {
            configureSerialization()
            configureAuthentication(testConfig)
            configureStatusPages()
            routing {
                authRoutes(testConfig)
                recipeRoutes()
            }
        }
        val client = createClient { install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) } }
        val token = client.post("/users/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginBody("user@test.com", "password123"))
        }.body<JsonObject>()["token"]!!.jsonPrimitive.content
        val response = client.post("/recipes") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(RecipeBody("", "some content"))
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun updateNonExistentRecipeReturns404() = testApplication {
        application {
            configureSerialization()
            configureAuthentication(testConfig)
            configureStatusPages()
            routing {
                authRoutes(testConfig)
                recipeRoutes()
            }
        }
        val client = createClient { install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) } }
        val token = client.post("/users/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginBody("user@test.com", "password123"))
        }.body<JsonObject>()["token"]!!.jsonPrimitive.content
        val response = client.put("/recipes/00000000-0000-0000-0000-000000000000") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(RecipeBody("Updated", "content"))
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun deleteNonExistentRecipeReturns404() = testApplication {
        application {
            configureSerialization()
            configureAuthentication(testConfig)
            configureStatusPages()
            routing {
                authRoutes(testConfig)
                recipeRoutes()
            }
        }
        val client = createClient { install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) } }
        val token = client.post("/users/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginBody("user@test.com", "password123"))
        }.body<JsonObject>()["token"]!!.jsonPrimitive.content
        val response = client.delete("/recipes/00000000-0000-0000-0000-000000000000") {
            bearerAuth(token)
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun updateRecipeWithInvalidUuidReturns400() = testApplication {
        application {
            configureSerialization()
            configureAuthentication(testConfig)
            configureStatusPages()
            routing {
                authRoutes(testConfig)
                recipeRoutes()
            }
        }
        val client = createClient { install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) } }
        val token = client.post("/users/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginBody("user@test.com", "password123"))
        }.body<JsonObject>()["token"]!!.jsonPrimitive.content
        val response = client.put("/recipes/not-a-valid-uuid") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(RecipeBody("Updated", "content"))
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun unknownRouteReturns404() = testApplication {
        application {
            configureSerialization()
            configureAuthentication(testConfig)
            configureStatusPages()
            routing { healthRoutes() }
        }
        val response = client.get("/completely-unknown-endpoint-xyz")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun configureCorsWithNullOriginsAndDevModeAllowsAnyHost() = testApplication {
        application {
            configureSerialization()
            configureCors(testConfig) // testConfig has devMode=true and corsAllowedOrigins=null
            routing { healthRoutes() }
        }
        val response = client.get("/health")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun configureCorsWithNullOriginsAndProdModeThrows() {
        val prodConfig = testConfig.copy(devMode = false, corsAllowedOrigins = null)
        var caughtException: IllegalStateException? = null
        try {
            testApplication {
                application {
                    configureSerialization()
                    configureCors(prodConfig)
                    routing { healthRoutes() }
                }
                // Trigger startup by making a request
                client.get("/health")
            }
        } catch (e: IllegalStateException) {
            caughtException = e
        }
        assertNotNull(caughtException)
        assertTrue(caughtException.message!!.contains("CORS_ALLOWED_ORIGINS"))
    }

    @Test
    fun configureCorsWithSpecificOriginsRestrictsToThoseHosts() = testApplication {
        val configWithCors = testConfig.copy(corsAllowedOrigins = listOf("http://example.com"))
        application {
            configureSerialization()
            configureCors(configWithCors)
            routing { healthRoutes() }
        }
        val response = client.get("/health") {
            header(HttpHeaders.Origin, "http://example.com")
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun configureRoutingSetsUpHealthAndRecipeEndpoints() = testApplication {
        application {
            configureSerialization()
            configureAuthentication(testConfig)
            configureStatusPages()
            configureRouting(testConfig)
        }
        val response = client.get("/health")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun updateRecipeChangesContent() = testApplication {
        application {
            configureSerialization()
            configureAuthentication(testConfig)
            configureStatusPages()
            routing {
                authRoutes(testConfig)
                recipeRoutes()
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
            setBody(RecipeBody("Original", "original content"))
        }.body<JsonObject>()["id"]!!.jsonPrimitive.content

        val updateResponse = client.put("/recipes/$recipeId") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(RecipeBody("Updated", "updated content"))
        }
        assertEquals(HttpStatusCode.OK, updateResponse.status)
        val updated = updateResponse.body<JsonObject>()
        assertEquals("Updated", updated["title"]!!.jsonPrimitive.content)
    }
}
