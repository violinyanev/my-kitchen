package com.ultraviolince.mykitchen.recipes.data.repository

import com.ultraviolince.mykitchen.recipes.domain.repository.AuthRepository
import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
import com.ultraviolince.mykitchen.recipes.domain.service.AuthService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AuthRepositoryImplTest {

    private val authService = mockk<AuthService>()
    private val authRepository: AuthRepository = AuthRepositoryImpl(authService)

    @Test
    fun `login should delegate to auth service`() = runTest {
        // Given
        coEvery { authService.login(any(), any(), any()) } returns Unit

        // When
        authRepository.login("server", "email", "password")

        // Then
        coVerify { authService.login("server", "email", "password") }
    }

    @Test
    fun `logout should delegate to auth service`() = runTest {
        // Given
        coEvery { authService.logout() } returns Unit

        // When
        authRepository.logout()

        // Then
        coVerify { authService.logout() }
    }

    @Test
    fun `getLoginState should return auth service login state`() = runTest {
        // Given
        val expectedState = LoginState.LoginSuccess
        every { authService.getLoginState() } returns flowOf(expectedState)

        // When
        val result = authRepository.getLoginState()

        // Then
        result.collect { state ->
            assert(state == expectedState)
        }
    }
}