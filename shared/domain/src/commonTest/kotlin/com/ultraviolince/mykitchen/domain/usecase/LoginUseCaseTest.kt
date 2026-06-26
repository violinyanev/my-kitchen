package com.ultraviolince.mykitchen.domain.usecase

import com.ultraviolince.mykitchen.domain.fake.FakeRecipeRepository
import com.ultraviolince.mykitchen.domain.model.AuthState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class LoginUseCaseTest {

    @Test
    fun `successful login transitions to LoggedIn state`() = runTest {
        val repo = FakeRecipeRepository()
        LoginUseCase(repo)("user@test.com", "pass", "http://localhost:5000")
        val state = repo.getAuthState().first()
        assertTrue(state is AuthState.LoggedIn)
    }

    @Test
    fun `returns failure for blank email`() = runTest {
        val result = LoginUseCase(FakeRecipeRepository())("", "pass", "http://localhost:5000")
        assertTrue(result.isFailure)
    }

    @Test
    fun `returns failure for blank server url`() = runTest {
        val result = LoginUseCase(FakeRecipeRepository())("a@b.com", "pass", "")
        assertTrue(result.isFailure)
    }

    @Test
    fun `returns failure when repo login fails`() = runTest {
        val repo = FakeRecipeRepository()
        repo.loginResult = Result.failure(Exception("unauthorized"))
        val result = LoginUseCase(repo)("a@b.com", "pass", "http://localhost:5000")
        assertTrue(result.isFailure)
    }
}
