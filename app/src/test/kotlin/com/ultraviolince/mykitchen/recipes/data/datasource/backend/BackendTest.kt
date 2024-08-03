package com.ultraviolince.mykitchen.recipes.data.datasource.backend

import com.ultraviolince.mykitchen.recipes.data.datasource.backend.data.BackendRecipe
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.data.BackendRecipeResponse
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.data.LoginRequest
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.data.LoginResult
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.data.LoginResultData
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.util.Result
import io.ktor.client.HttpClient
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

class BackendTest {

    companion object {
        const val HOST = "https://test.com"
        const val USER = "User"
        const val TOKEN = "MyToken"
        const val EMAIL = "me@example.com"
        const val PASSWORD = "123456 :)"
    }

    private fun createHttpClientForTests(mockEngine: MockEngine): HttpClient {
        return createHttpClient(mockEngine, MOCK_HOST, null)
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
        val recipeService = RecipeService(createHttpClientForTests(mockEngine))

        val response = recipeService.login(LoginRequest(email = EMAIL, password = PASSWORD))

        assertEquals(Result.Success(LoginResult(
            LoginResultData(
                username = USER,
                token = TOKEN
            )
        )), response)

        assertEquals(mockEngine.requestHistory.size, 1)
        val request = mockEngine.requestHistory[0]
        assertEquals(Url("$MOCK_HOST/users/login"), request.url)
        assertEquals(HttpMethod.Post, request.method)
        assertEquals("""{"email":"$EMAIL","password":"$PASSWORD"}""", request.body.toByteArray().toString(Charsets.UTF_8))
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
        val recipeService = RecipeService(createHttpClientForTests(mockEngine))

        val response = recipeService.getRecipes()

        assertEquals(response, Result.Success(
            listOf(
                BackendRecipe(
                    id = 1L,
                    timestamp = 11L,
                    title = "r1",
                    body = "b"
                )
            )
        ))

        assertEquals(mockEngine.requestHistory.size, 1)
        val request = mockEngine.requestHistory[0]
        assertEquals(Url("$MOCK_HOST/recipes"), request.url)
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
        val recipeService = RecipeService(createHttpClientForTests(mockEngine))
        val recipe = BackendRecipe(
            id = 1L,
            title = "title",
            body = "body",
            timestamp = 5L
        )

        val response = recipeService.createRecipe(
            recipeRequest = recipe
        )

        assertEquals(response, Result.Success(
            BackendRecipeResponse(recipe)
        ))

        assertEquals(mockEngine.requestHistory.size, 1)
        val request = mockEngine.requestHistory[0]
        assertEquals(Url("$MOCK_HOST/recipes"), request.url)
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
        val recipeService = RecipeService(createHttpClientForTests(mockEngine))
        val response = recipeService.deleteRecipe(recipeId = 5L)

        assertEquals(response, Result.Success(Unit))

        assertEquals(mockEngine.requestHistory.size, 1)
        val request = mockEngine.requestHistory[0]
        assertEquals(Url("$MOCK_HOST/recipes/5"), request.url)
        assertEquals(HttpMethod.Delete, request.method)
        assertEquals(0L, request.body.contentLength)
    }
}
