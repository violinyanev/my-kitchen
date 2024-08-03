package com.ultraviolince.mykitchen.recipes.data

import com.ultraviolince.mykitchen.recipes.data.datasource.backend.RecipeServiceWrapper
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.util.NetworkError
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipeServiceWrapperTest {

    private val service = RecipeServiceWrapper()

    // TODO Fix the tests
    @Test
    fun logsInToServerSuccessfully() = runTest {
        assertEquals(
            LoginState.LoginSuccess,
            service.login(
                FakeBackend.server,
                email = FakeBackend.testUser,
                FakeBackend.testPassword
            )
        )
    }

    @Test
    fun failsToLoginToServerWhenServerUriIsMalformed() = runTest {
        val result = service.login(
            "not valid address",
            email = FakeBackend.testUser,
            FakeBackend.testPassword
        )

        assertEquals(LoginState.LoginFailure(NetworkError.SERVER_ERROR), result)
    }

    @Test
    fun failsToLoginToServerWhenPasswordIsWrong() = runTest {
        val result = service.login(FakeBackend.server, email = FakeBackend.testUser, "bad password")
        // TODO Probbly needs different error handling
        assertEquals(LoginState.LoginFailure(NetworkError.UNAUTHORIZED), result)
    }

    @Test
    fun testRecipesCreateDelete() = runTest {
        val fakeId = 5L

        val result = service.login(
            FakeBackend.server,
            email = FakeBackend.testUser,
            FakeBackend.testPassword
        )
        assertEquals(result, LoginState.LoginSuccess)

        val created = service.insertRecipe(
            recipeId = fakeId,
            recipe = Recipe(
                id = fakeId,
                title = "title",
                content = "body",
                timestamp = 5L
            )
        )

        assertTrue(created)

        val deleted = service.deleteRecipe(
            recipeId = fakeId
        )
        assertTrue(deleted)
    }
}
