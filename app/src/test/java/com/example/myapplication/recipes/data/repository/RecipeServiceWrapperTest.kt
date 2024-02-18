package com.example.myapplication.recipes.data.repository

import com.example.myapplication.recipes.data.datasource.backend.RecipeServiceWrapper
import com.example.myapplication.recipes.domain.repository.LoginState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RecipeServiceWrapperTest {

    private val service = RecipeServiceWrapper()

    @Test
    fun `logs in to server successfully`() = runTest {
        assertEquals(service.login(FakeBackend.server, email = FakeBackend.testUser, FakeBackend.testPassword), LoginState.LoginSuccess)
    }

    @Test
    fun `fails to log in to backend when the server url is wrong`() = runTest {
        val result = service.login("not valid address", email = FakeBackend.testUser, FakeBackend.testPassword)
        assertEquals(result, LoginState.LoginFailure("Malformed server URL! Use http(s)://yourdomain.com(:port)"))
    }

    @Test
    fun `fails to log in to backend when password is wrong`() = runTest {
        val result = service.login(FakeBackend.server, email = FakeBackend.testUser, "bad password")
        assertEquals(result, LoginState.LoginFailure("Bad credentials!"))
    }
}

