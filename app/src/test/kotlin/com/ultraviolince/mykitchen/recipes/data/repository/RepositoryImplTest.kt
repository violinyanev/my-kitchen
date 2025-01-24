package com.ultraviolince.mykitchen.recipes.data.repository

import com.ultraviolince.mykitchen.recipes.data.datasource.backend.RecipeServiceWrapper
import com.ultraviolince.mykitchen.recipes.data.datasource.datastore.SafeDataStore
import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RepositoryImplTest {

//    private val recipesDao = mockk<RecipeDao>()

    private val dataStoreMock = mockk<SafeDataStore>()

    private val service = RecipeServiceWrapper(dataStoreMock)

    @Test
    fun `not logged in by default`() = runTest {
        val loginState = service.loginState

        assertEquals(LoginState.LoginEmpty, loginState.first())
    }

//    @Test
//    fun `logs in to server successfully`() = runTest {
//        val loginState = service.loginState
//
//        coEvery { serviceMock.login(any(), any(), any()) } returns LoginState.LoginSuccess
//        coEvery { serviceMock.sync(any()) }
//
//        loginState.test {
//            service.login("a", "b", "c")
//            assertEquals(awaitItem(), LoginState.LoginEmpty)
//            assertEquals(awaitItem(), LoginState.LoginPending)
//            assertEquals(awaitItem(), LoginState.LoginSuccess)
//        }
//
//        coVerify { serviceMock.login("a", "b", "c") }
//        coVerify { serviceMock.sync(recipesDao) }
//    }
//
//    @Test
//    fun `fails to log in to backend when the backend reports errors`() = runTest {
//        val loginState = repository.getLoginState()
//
//        coEvery { serviceMock.login(any(), any(), any()) } returns LoginState.LoginFailure(NetworkError.NO_INTERNET)
//
//        loginState.test {
//            repository.login("a", "b", "c")
//            assertEquals(awaitItem(), LoginState.LoginEmpty)
//            assertEquals(awaitItem(), LoginState.LoginPending)
//            assertEquals(awaitItem(), LoginState.LoginFailure(NetworkError.NO_INTERNET))
//        }
//
//        coVerify { serviceMock.login("a", "b", "c") }
//    }
}
