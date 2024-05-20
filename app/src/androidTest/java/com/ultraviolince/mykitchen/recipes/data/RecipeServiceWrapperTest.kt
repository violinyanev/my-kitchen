package com.ultraviolince.mykitchen.recipes.data

import com.ultraviolince.mykitchen.R
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.RecipeServiceWrapper
import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
    fun failsToLoginToServerWhenEmailIsWrong() = runTest {
        val result = service.login(
            "not valid address",
            email = FakeBackend.testUser,
            FakeBackend.testPassword
        )
        assertEquals(result, LoginState.LoginFailure(R.string.malformed_server_uri))
    }

    @Test
    fun failsToLoginToServerWhenPasswordIsWrong() = runTest {
        val result = service.login(FakeBackend.server, email = FakeBackend.testUser, "bad password")
        assertEquals(result, LoginState.LoginFailure(R.string.wrong_credentials))
    }
}
