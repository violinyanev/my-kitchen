package com.ultraviolince.mykitchen.recipes.data.repository

import com.ultraviolince.mykitchen.recipes.domain.repository.AuthRepository
import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
import com.ultraviolince.mykitchen.recipes.domain.service.AuthService
import kotlinx.coroutines.flow.Flow

class AuthRepositoryImpl(
    private val authService: AuthService
) : AuthRepository {

    override suspend fun login(server: String, email: String, password: String) {
        authService.login(server, email, password)
    }

    override suspend fun logout() {
        authService.logout()
    }

    override fun getLoginState(): Flow<LoginState> {
        return authService.getLoginState()
    }
}
