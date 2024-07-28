package com.ultraviolince.mykitchen.recipes.data

import com.ultraviolince.mykitchen.R
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.RecipeServiceWrapper
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipeServiceWrapperTest {

    private val service = RecipeServiceWrapper()

    @Test
    fun logsInToServerSuccessfully() = runTest {
        assertEquals(
            service.login(
                FakeBackend.server,
                email = FakeBackend.testUser,
                FakeBackend.testPassword
            ),
            LoginState.LoginSuccess
        )
    }

    @Test
    fun failsToLoginToServerWhenServerUriIsMalformed() = runTest {
        val result = service.login(
            "not valid address",
            email = FakeBackend.testUser,
            FakeBackend.testPassword
        )

        // TODO fix this
        // assertEquals(result, LoginState.LoginFailure(R.string.malformed_server_uri))
        assertEquals(result, LoginState.LoginFailure(R.string.unknown_error))
    }

    @Test
    fun failsToLoginToServerWhenPasswordIsWrong() = runTest {
        val result = service.login(FakeBackend.server, email = FakeBackend.testUser, "bad password")
        // TODO fix this
        // assertEquals(result, LoginState.LoginFailure(R.string.wrong_credentials))
        assertEquals(result, LoginState.LoginFailure(R.string.unknown_error))
    }

    @Test
    fun testRecipesCreateDelete() = runTest {
        val result = service.login(
            FakeBackend.server,
            email = FakeBackend.testUser,
            FakeBackend.testPassword
        )
        assertEquals(result, LoginState.LoginSuccess)

        val created = service.insertRecipe(
            recipeId = 123L,
            recipe = Recipe(
                id = 0L,
                title = "title",
                content = "body",
                timestamp = 5L
            )
        )

        assertTrue(created)

        val deleted = service.deleteRecipe(
            recipeId = 123L
        )
        assertTrue(deleted)
    }
}
