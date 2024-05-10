package com.ultraviolince.mykitchen.recipes.data.datasource.backend

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class BackendTest {

    companion object {
        const val user = "User"
        const val token = "MyToken"
        const val email = "me@example.com"
        const val password = "123456 :)"
    }

    private val mockWebServer = MockWebServer()
    private lateinit var recipeService: RecipeService

    @Before
    fun setup() {
        mockWebServer.start()
        recipeService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RecipeService::class.java)
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `logs in successfully`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setBody("{\"data\":{\"email\":\"$email\",\"token\":\"$token\",\"username\":\"$user\"}}")
        )

        val response = recipeService.login(LoginRequest(email = user, password = password))

        assertEquals(response.data.username, user)
        assertEquals(response.data.token, token)

        val request = mockWebServer.takeRequest()
        assertEquals("/users/login", request.path)
        assertEquals("{\"email\":\"$user\",\"password\":\"$password\"}", request.body.readUtf8())
        assertNull(request.getHeader("Authorization"))
    }

    @Test
    fun `gets list of recipes`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setBody("[" +
                    "{\"body\":\"b\",\"id\":1,\"timestamp\":11,\"title\":\"r1\",\"user\":\"u1\"}" +
                    "]")
        )

        val response = recipeService.getRecipes()

        assertEquals(response.size, 1)
        assertEquals(response[0].body, "b")
        assertEquals(response[0].title, "r1")
        assertEquals(response[0].timestamp, 11L)
        assertEquals(response[0].id, 1L)
        // TODO: test author?

        val request = mockWebServer.takeRequest()
        assertEquals("/recipes", request.path)
        assertEquals("", request.body.readUtf8())
    }

    @Test
    fun `creates a recipe`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setBody("{\"data\":{\"content\":\"c\",\"title\":\"t\",\"timestamp\":5,\"id\":1}}")
        )
        val response = recipeService.createRecipe(recipeRequest = BackendRecipe(
            id = 1L, title = "title", body = "body", timestamp = 5L
        )
        )

        // TODO: make this a proper response
        assertEquals(response.recipe, null)
//
        val request = mockWebServer.takeRequest()
        assertEquals("/recipes", request.path)
        assertEquals("{\"id\":1,\"title\":\"title\",\"body\":\"body\",\"timestamp\":5}", request.body.readUtf8())
    }

    @Test
    fun `deletes a recipe`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setBody("")
        )
        val response = recipeService.deleteRecipe(recipeId = 5L)

        // TODO: make this a proper response
        assertEquals(response.body(), Unit)

        val request = mockWebServer.takeRequest()
        assertEquals("/recipes/5", request.path)
        assertEquals("", request.body.readUtf8())
    }
}
