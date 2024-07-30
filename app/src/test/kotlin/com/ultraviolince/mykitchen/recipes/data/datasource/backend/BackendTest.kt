package com.ultraviolince.mykitchen.recipes.data.datasource.backend

import com.ultraviolince.mykitchen.recipes.data.datasource.backend.data.BackendRecipe
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.data.LoginRequest
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BackendTest {

    companion object {
        const val HOST = "https://test.com"
        const val USER = "User"
        const val TOKEN = "MyToken"
        const val EMAIL = "me@example.com"
        const val PASSWORD = "123456 :)"
    }

    @Test
    fun `logs in successfully`() = runTest {
        val mockEngine = MockEngine {
            respond(
                content = ByteReadChannel("""{"data":{"email":"$EMAIL","token":"$TOKEN","username":"$USER"}}"""),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val recipeService = RecipeService(createHttpClient(mockEngine, HOST, null))

        val response = recipeService.login(LoginRequest(email = USER, password = PASSWORD))

        assertTrue(response.isSuccess)

        val loginResult = response.getOrThrow()
        assertEquals(loginResult.data.username, USER)
        assertEquals(loginResult.data.token, TOKEN)

        assertEquals(mockEngine.requestHistory.size, 1)
        val request = mockEngine.requestHistory[0]
        assertEquals(Url("$HOST/users/login"), request.url)
        assertEquals(HttpMethod.Post, request.method)
        assertEquals("""{"email":"$USER","password":"$PASSWORD"}""", request.body.toByteArray().toString(Charsets.UTF_8))
    }

    @Test
    fun `gets list of recipes`() = runTest {
        val mockEngine = MockEngine {
            respond(
                content = ByteReadChannel("""[{"body":"b","id":1,"timestamp":11,"title":"r1","user":"u1"}]"""),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val recipeService = RecipeService(createHttpClient(mockEngine, HOST, null))

        val response = recipeService.getRecipes().getOrThrow()

        assertEquals(response.size, 1)
        assertEquals(response[0].body, "b")
        assertEquals(response[0].title, "r1")
        assertEquals(response[0].timestamp, 11L)
        assertEquals(response[0].id, 1L)
        // TODO: test author?

        assertEquals(mockEngine.requestHistory.size, 1)
        val request = mockEngine.requestHistory[0]
        assertEquals(Url("$HOST/recipes"), request.url)
        assertEquals(HttpMethod.Get, request.method)
        assertEquals(0L, request.body.contentLength)
    }

    @Test
    fun `creates a recipe`() = runTest {
        val mockEngine = MockEngine {
            respond(
                content = ByteReadChannel("""{"message":"Recipe created successfully","recipe":{"body":"body","id":1,"timestamp":5,"title":"title","user":"u"}}"""),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val recipeService = RecipeService(createHttpClient(mockEngine, HOST, null))
        val response = recipeService.createRecipe(
            recipeRequest = BackendRecipe(
                id = 1L,
                title = "title",
                body = "body",
                timestamp = 5L
            )
        ).getOrThrow()

        assertEquals(response.recipe.title, "title")
        assertEquals(response.recipe.body, "body")
        assertEquals(response.recipe.timestamp, 5L)
        assertEquals(response.recipe.id, 1L)

        assertEquals(mockEngine.requestHistory.size, 1)
        val request = mockEngine.requestHistory[0]
        assertEquals(Url("$HOST/recipes"), request.url)
        assertEquals(HttpMethod.Post, request.method)
        assertEquals("""{"id":1,"title":"title","body":"body","timestamp":5}""", request.body.toByteArray().toString(Charsets.UTF_8))
    }

    @Test
    fun `deletes a recipe`() = runTest {
        val mockEngine = MockEngine {
            respond(
                content = ByteReadChannel(""),
                status = HttpStatusCode.NoContent,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val recipeService = RecipeService(createHttpClient(mockEngine, HOST, null))
        val response = recipeService.deleteRecipe(recipeId = 5L).getOrThrow()

        assertNotNull(response)

        assertEquals(mockEngine.requestHistory.size, 1)
        val request = mockEngine.requestHistory[0]
        assertEquals(Url("$HOST/recipes/5"), request.url)
        assertEquals(HttpMethod.Delete, request.method)
        assertEquals(0L, request.body.contentLength)
    }
}
