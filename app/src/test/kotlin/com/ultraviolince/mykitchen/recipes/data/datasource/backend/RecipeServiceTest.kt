package com.ultraviolince.mykitchen.recipes.data.datasource.backend

import com.google.common.truth.Truth.assertThat
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.data.BackendRecipe
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.data.LoginRequest
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.util.NetworkError
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.util.Result
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.junit.Test

class RecipeServiceTest {

    private fun createMockHttpClient(statusCode: HttpStatusCode, responseBody: String = "[]"): HttpClient {
        val mockEngine = MockEngine {
            respond(
                content = responseBody,
                status = statusCode,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        return HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    private fun createErrorMockHttpClient(exception: Exception): HttpClient {
        val mockEngine = MockEngine {
            throw exception
        }
        return HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    @Test
    fun `getRecipes returns success with valid response`() = runBlocking {
        // Given
        val responseBody = """[{"id": 1, "title": "Recipe 1", "body": "Content 1", "timestamp": 123}]"""
        val httpClient = createMockHttpClient(HttpStatusCode.OK, responseBody)
        val recipeService = RecipeService(httpClient)

        // When
        val result = recipeService.getRecipes()

        // Then
        assertThat(result).isInstanceOf(Result.Success::class.java)
        val recipes = (result as Result.Success).data
        assertThat(recipes).hasSize(1)
        assertThat(recipes[0].title).isEqualTo("Recipe 1")
    }

    @Test
    fun `getRecipes returns unauthorized error for 401 status`() = runBlocking {
        // Given
        val httpClient = createMockHttpClient(HttpStatusCode.Unauthorized)
        val recipeService = RecipeService(httpClient)

        // When
        val result = recipeService.getRecipes()

        // Then
        assertThat(result).isInstanceOf(Result.Error::class.java)
        assertThat((result as Result.Error).error).isEqualTo(NetworkError.UNAUTHORIZED)
    }

    @Test
    fun `getRecipes returns server error for 500 status`() = runBlocking {
        // Given
        val httpClient = createMockHttpClient(HttpStatusCode.InternalServerError)
        val recipeService = RecipeService(httpClient)

        // When
        val result = recipeService.getRecipes()

        // Then
        assertThat(result).isInstanceOf(Result.Error::class.java)
        assertThat((result as Result.Error).error).isEqualTo(NetworkError.SERVER_ERROR)
    }

    @Test
    fun `getRecipes returns no internet error for UnresolvedAddressException`() = runBlocking {
        // Given
        val httpClient = createErrorMockHttpClient(UnresolvedAddressException())
        val recipeService = RecipeService(httpClient)

        // When
        val result = recipeService.getRecipes()

        // Then
        assertThat(result).isInstanceOf(Result.Error::class.java)
        assertThat((result as Result.Error).error).isEqualTo(NetworkError.NO_INTERNET)
    }

    @Test
    fun `getRecipes returns serialization error for SerializationException`() = runBlocking {
        // Given
        val httpClient = createErrorMockHttpClient(SerializationException("Invalid JSON"))
        val recipeService = RecipeService(httpClient)

        // When
        val result = recipeService.getRecipes()

        // Then
        assertThat(result).isInstanceOf(Result.Error::class.java)
        assertThat((result as Result.Error).error).isEqualTo(NetworkError.SERIALIZATION)
    }

    @Test
    fun `createRecipe returns success with valid response`() = runBlocking {
        // Given
        val responseBody = """{"recipe": {"id": 1, "title": "Recipe 1", "body": "Content", "timestamp": 123}}"""
        val httpClient = createMockHttpClient(HttpStatusCode.Created, responseBody)
        val recipeService = RecipeService(httpClient)
        val recipe = BackendRecipe(1L, "Recipe 1", "Content", 123L)

        // When
        val result = recipeService.createRecipe(recipe)

        // Then
        assertThat(result).isInstanceOf(Result.Success::class.java)
        val response = (result as Result.Success).data
        assertThat(response.recipe.title).isEqualTo("Recipe 1")
    }

    @Test
    fun `createRecipe returns unauthorized error for 401 status`() = runBlocking {
        // Given
        val httpClient = createMockHttpClient(HttpStatusCode.Unauthorized)
        val recipeService = RecipeService(httpClient)
        val recipe = BackendRecipe(1L, "Recipe 1", "Content", 123L)

        // When
        val result = recipeService.createRecipe(recipe)

        // Then
        assertThat(result).isInstanceOf(Result.Error::class.java)
        assertThat((result as Result.Error).error).isEqualTo(NetworkError.UNAUTHORIZED)
    }

    @Test
    fun `createRecipe returns unknown error for 400 status`() = runBlocking {
        // Given
        val httpClient = createMockHttpClient(HttpStatusCode.BadRequest)
        val recipeService = RecipeService(httpClient)
        val recipe = BackendRecipe(1L, "Recipe 1", "Content", 123L)

        // When
        val result = recipeService.createRecipe(recipe)

        // Then
        assertThat(result).isInstanceOf(Result.Error::class.java)
        assertThat((result as Result.Error).error).isEqualTo(NetworkError.UNKNOWN)
    }

    @Test
    fun `createRecipe returns conflict error for 409 status`() = runBlocking {
        // Given
        val httpClient = createMockHttpClient(HttpStatusCode.Conflict)
        val recipeService = RecipeService(httpClient)
        val recipe = BackendRecipe(1L, "Recipe 1", "Content", 123L)

        // When
        val result = recipeService.createRecipe(recipe)

        // Then
        assertThat(result).isInstanceOf(Result.Error::class.java)
        assertThat((result as Result.Error).error).isEqualTo(NetworkError.CONFLICT)
    }

    @Test
    fun `createRecipe returns no internet error for UnresolvedAddressException`() = runBlocking {
        // Given
        val httpClient = createErrorMockHttpClient(UnresolvedAddressException())
        val recipeService = RecipeService(httpClient)
        val recipe = BackendRecipe(1L, "Recipe 1", "Content", 123L)

        // When
        val result = recipeService.createRecipe(recipe)

        // Then
        assertThat(result).isInstanceOf(Result.Error::class.java)
        assertThat((result as Result.Error).error).isEqualTo(NetworkError.NO_INTERNET)
    }

    @Test
    fun `deleteRecipe returns success for valid response`() = runBlocking {
        // Given
        val httpClient = createMockHttpClient(HttpStatusCode.NoContent)
        val recipeService = RecipeService(httpClient)

        // When
        val result = recipeService.deleteRecipe(1L)

        // Then
        assertThat(result).isInstanceOf(Result.Success::class.java)
        assertThat((result as Result.Success).data).isEqualTo(Unit)
    }

    @Test
    fun `deleteRecipe returns unauthorized error for 401 status`() = runBlocking {
        // Given
        val httpClient = createMockHttpClient(HttpStatusCode.Unauthorized)
        val recipeService = RecipeService(httpClient)

        // When
        val result = recipeService.deleteRecipe(1L)

        // Then
        assertThat(result).isInstanceOf(Result.Error::class.java)
        assertThat((result as Result.Error).error).isEqualTo(NetworkError.UNAUTHORIZED)
    }

    @Test
    fun `deleteRecipe returns conflict error for 409 status`() = runBlocking {
        // Given
        val httpClient = createMockHttpClient(HttpStatusCode.Conflict)
        val recipeService = RecipeService(httpClient)

        // When
        val result = recipeService.deleteRecipe(1L)

        // Then
        assertThat(result).isInstanceOf(Result.Error::class.java)
        assertThat((result as Result.Error).error).isEqualTo(NetworkError.CONFLICT)
    }

    @Test
    fun `deleteRecipe returns server error for 500 status`() = runBlocking {
        // Given
        val httpClient = createMockHttpClient(HttpStatusCode.InternalServerError)
        val recipeService = RecipeService(httpClient)

        // When
        val result = recipeService.deleteRecipe(1L)

        // Then
        assertThat(result).isInstanceOf(Result.Error::class.java)
        assertThat((result as Result.Error).error).isEqualTo(NetworkError.SERVER_ERROR)
    }

    @Test
    fun `deleteRecipe returns unknown error for unhandled status`() = runBlocking {
        // Given
        val httpClient = createMockHttpClient(HttpStatusCode.Forbidden)
        val recipeService = RecipeService(httpClient)

        // When
        val result = recipeService.deleteRecipe(1L)

        // Then
        assertThat(result).isInstanceOf(Result.Error::class.java)
        assertThat((result as Result.Error).error).isEqualTo(NetworkError.UNKNOWN)
    }

    @Test
    fun `login returns success with valid credentials`() = runBlocking {
        // Given
        val responseBody = """{"data": {"token": "abc123", "username": "user@example.com"}}"""
        val httpClient = createMockHttpClient(HttpStatusCode.OK, responseBody)
        val recipeService = RecipeService(httpClient)
        val loginRequest = LoginRequest("user@example.com", "password")

        // When
        val result = recipeService.login(loginRequest)

        // Then
        assertThat(result).isInstanceOf(Result.Success::class.java)
        val loginResult = (result as Result.Success).data
        assertThat(loginResult.data.token).isEqualTo("abc123")
        assertThat(loginResult.data.username).isEqualTo("user@example.com")
    }

    @Test
    fun `login returns server error for ConnectException`() = runBlocking {
        // Given
        val httpClient = createErrorMockHttpClient(java.net.ConnectException("Connection refused"))
        val recipeService = RecipeService(httpClient)
        val loginRequest = LoginRequest("user@example.com", "password")

        // When
        val result = recipeService.login(loginRequest)

        // Then
        assertThat(result).isInstanceOf(Result.Error::class.java)
        assertThat((result as Result.Error).error).isEqualTo(NetworkError.SERVER_ERROR)
    }
}
