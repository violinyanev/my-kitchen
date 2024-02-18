package com.example.myapplication.recipes.data.repository

import com.example.myapplication.recipes.data.datasource.backend.BackendRecipe
import com.example.myapplication.recipes.data.datasource.backend.LoginRequest
import com.example.myapplication.recipes.data.datasource.backend.RecipeService
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthInterceptor(private val authToken: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val modifiedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $authToken")
            .build()
        return chain.proceed(modifiedRequest)
    }
}

class BackendTest {

    private val server = "https://ultraviolince.com:8019"
    private val testToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6InRlc3R1c2VyIn0.hgvrPuJ1j0PlnnsvYD2mHiFpDycfMgvPYd6ilI3wX78"

    private val recipeService = Retrofit.Builder()
        .baseUrl(server)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(RecipeService::class.java)

    private val httpClient = OkHttpClient.Builder().addInterceptor(AuthInterceptor(testToken)).build()

    private val authenticatedService = Retrofit.Builder()
        .baseUrl(server)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(RecipeService::class.java)

    @Test
    fun `logs in with test user`() = runTest {
        val response = recipeService.login(LoginRequest(email = "test@user.com", password = "TestPassword")).execute()

        assertTrue(response.isSuccessful)
        val data = response.body()!!.data

        assertEquals(data.username, "testuser")
        assertEquals(data.token, testToken)
    }

    @Test
    fun `has empty list of recipes`() = runTest {
        val response = authenticatedService.getRecipes().execute()

        assertTrue(response.isSuccessful)
        val body = response.body()!!

        assertTrue(body.isEmpty())
    }

    @Test
    fun `creates and deletes a recipe`() = runTest {
        val id = 123L
        val createResponse = authenticatedService.createRecipe(BackendRecipe(id = id, title = "test", body = "body", timestamp = 567L)).execute()

        assertTrue(createResponse.isSuccessful)

        val body = createResponse.body()!!
        assertEquals("test", body.recipe.title)
        assertEquals("body", body.recipe.body)
        assertEquals(id, body.recipe.id)
        assertEquals(567L, body.recipe.timestamp)

        val deleteResponse = authenticatedService.deleteRecipe(recipeId = id).execute()

        assertTrue(deleteResponse.isSuccessful)
    }
}
