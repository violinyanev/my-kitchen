package com.ultraviolince.mykitchen.domain.usecase

import com.ultraviolince.mykitchen.domain.fake.FakeRecipeRepository
import com.ultraviolince.mykitchen.domain.model.AuthState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetAuthStateUseCaseTest {

    @Test
    fun `returns LoggedOut initially`() = runTest {
        val useCase = GetAuthStateUseCase(FakeRecipeRepository())
        assertEquals(AuthState.LoggedOut, useCase().first())
    }

    @Test
    fun `returns LoggedIn after successful login`() = runTest {
        val repo = FakeRecipeRepository()
        val useCase = GetAuthStateUseCase(repo)
        repo.login("user@test.com", "pass", "http://localhost:5000")
        assertTrue(useCase().first() is AuthState.LoggedIn)
    }
}
