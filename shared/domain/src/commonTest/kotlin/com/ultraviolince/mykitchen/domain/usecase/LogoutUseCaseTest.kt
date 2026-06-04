package com.ultraviolince.mykitchen.domain.usecase

import com.ultraviolince.mykitchen.domain.fake.FakeRecipeRepository
import com.ultraviolince.mykitchen.domain.model.AuthState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LogoutUseCaseTest {

    @Test
    fun `logout transitions to LoggedOut state`() = runTest {
        val repo = FakeRecipeRepository()
        LoginUseCase(repo)("user@test.com", "pass", "http://localhost:5000")
        LogoutUseCase(repo)()
        val state = repo.getAuthState().first()
        assertEquals(AuthState.LoggedOut, state)
    }

    @Test
    fun `logout from already-logged-out state is a no-op`() = runTest {
        val repo = FakeRecipeRepository()
        LogoutUseCase(repo)() // Should not throw
        val state = repo.getAuthState().first()
        assertEquals(AuthState.LoggedOut, state)
    }
}
