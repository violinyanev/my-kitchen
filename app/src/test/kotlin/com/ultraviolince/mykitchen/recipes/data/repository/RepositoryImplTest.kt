package com.ultraviolince.mykitchen.recipes.data.repository

import app.cash.turbine.test
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.RecipeServiceWrapper
import com.ultraviolince.mykitchen.recipes.data.datasource.localdb.RecipeDao
import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RepositoryImplTest {

    private val recipesDao = mockk<RecipeDao>()

    private val serviceMock = mockk<RecipeServiceWrapper>()

    private val repository = RecipeRepositoryImpl(recipesDao, serviceMock)

    @Test
    fun `not logged in by default`() = runTest {
        val loginState = repository.getLoginState()

        assertEquals(LoginState.LoginEmpty, loginState.first())
    }

    // TODO fix this test
    /*@Test
    fun `logs in to server successfully`() = runTest {
        val loginState = repository.getLoginState()

        coEvery { serviceMock.login(any(), any(), any()) } returns LoginState.LoginSuccess
        coEvery { serviceMock.sync(any()) }

        loginState.test {
            repository.login("a", "b", "c")
            assertEquals(awaitItem(), LoginState.LoginEmpty)
            assertEquals(awaitItem(), LoginState.LoginPending)
            assertEquals(awaitItem(), LoginState.LoginSuccess)
        }

        coVerify { serviceMock.login("a", "b", "c") }
        coVerify { serviceMock.sync(recipesDao) }
    }*/

    @Test
    fun `fails to log in to backend when the backend reports errors`() = runTest {
        val loginState = repository.getLoginState()

        coEvery { serviceMock.login(any(), any(), any()) } returns LoginState.LoginFailure(5)

        loginState.test {
            repository.login("a", "b", "c")
            assertEquals(awaitItem(), LoginState.LoginEmpty)
            assertEquals(awaitItem(), LoginState.LoginPending)
            assertEquals(awaitItem(), LoginState.LoginFailure(5))
        }

        coVerify { serviceMock.login("a", "b", "c") }
    }
}
