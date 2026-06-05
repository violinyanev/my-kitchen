package com.ultraviolince.mykitchen.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecipeApiClientTest {

    private fun buildClient(mockEngine: MockEngine): HttpClient = HttpClient(mockEngine) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    @Test
    fun loginReturnsTokenOnSuccess() = runTest {
        val engine = MockEngine { _ ->
            respond(
                content = """{"token":"jwt-abc"}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val client = RecipeApiClient(buildClient(engine))
        val result = client.login("http://localhost:5000", "user@test.com", "pass")
        assertTrue(result.isSuccess)
        assertEquals("jwt-abc", result.getOrNull()?.token)
    }

    @Test
    fun loginReturnsFailureOn401() = runTest {
        val engine = MockEngine { _ ->
            respond(
                content = """{"error":"unauthorized"}""",
                status = HttpStatusCode.Unauthorized,
            )
        }
        val client = RecipeApiClient(buildClient(engine))
        val result = client.login("http://localhost:5000", "bad@user.com", "wrong")
        assertTrue(result.isFailure)
    }

    @Test
    fun getRecipesReturnsListOnSuccess() = runTest {
        val recipesJson =
            """[{"id":"1","title":"Pasta","content":"Boil","created_at":1000,"updated_at":1000}]"""
        val engine = MockEngine { _ ->
            respond(
                content = recipesJson,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val client = RecipeApiClient(buildClient(engine))
        val result = client.getRecipes("http://localhost:5000", "tok")
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("Pasta", result.getOrNull()?.first()?.title)
    }

    @Test
    fun deleteRecipeReturnsSuccessOn200() = runTest {
        val engine = MockEngine { _ ->
            respond(content = "", status = HttpStatusCode.OK)
        }
        val client = RecipeApiClient(buildClient(engine))
        val result = client.deleteRecipe("http://localhost:5000", "tok", "recipe-id")
        assertTrue(result.isSuccess)
    }
}
