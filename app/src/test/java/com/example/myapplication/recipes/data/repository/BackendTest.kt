package com.example.myapplication.recipes.data.repository

import com.example.myapplication.recipes.data.datasource.backend.AuthInterceptor
import com.example.myapplication.recipes.data.datasource.backend.BackendRecipe
import com.example.myapplication.recipes.data.datasource.backend.LoginRequest
import com.example.myapplication.recipes.data.datasource.backend.RecipeService
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BackendTest {

    private val recipeService = Retrofit.Builder()
        .baseUrl(FakeBackend.server)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(RecipeService::class.java)

    private val authenticatedService = Retrofit.Builder()
        .baseUrl(FakeBackend.server)
        .client(OkHttpClient.Builder().addInterceptor(AuthInterceptor(FakeBackend.testToken)).build())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(RecipeService::class.java)

    @Test
    fun `logs in with test user`() = runTest {
        val response = recipeService.login(LoginRequest(email = FakeBackend.testUser, password = FakeBackend.testPassword))

        assertEquals(response.data.username, FakeBackend.testUserName)
        assertEquals(response.data.token, FakeBackend.testToken)
    }

    @Test
    fun `fails to log in when email is not found`() {
        assertThrows(retrofit2.HttpException::class.java) {
            runTest {
                recipeService.login(LoginRequest(email = "wrong", password = FakeBackend.testPassword))
            }
        }
    }

    @Test
    fun `fails to log in with test user when password is wrong`() {
        assertThrows(retrofit2.HttpException::class.java) {
            runTest {
                recipeService.login(LoginRequest(email = FakeBackend.testUser, password = "wrong password"))
            }
        }
    }

    @Test
    fun `fails to list recipes when not authenticated`() {
        assertThrows(retrofit2.HttpException::class.java) {
            runTest {
                recipeService.getRecipes()
            }
        }
    }

    @Test
    fun `fails to create a recipe when not authenticated`() {
        assertThrows(retrofit2.HttpException::class.java) {
            runTest {
                recipeService.createRecipe(BackendRecipe(id = 3, title = "test", body = "body", timestamp = 567L))
            }
        }
    }

    @Test
    fun `gets list of recipes`() = runTest {
        // TODO check list is empty after mock server is used
        authenticatedService.getRecipes()
    }

    @Test
    fun `creates and deletes a recipe`() = runTest {
        val id = 123L
        val createResponse = authenticatedService.createRecipe(BackendRecipe(id = id, title = "test", body = "body", timestamp = 567L))

        assertEquals("test", createResponse.recipe.title)
        assertEquals("body", createResponse.recipe.body)
        assertEquals(id, createResponse.recipe.id)
        assertEquals(567L, createResponse.recipe.timestamp)

        authenticatedService.deleteRecipe(recipeId = id)
    }
}
