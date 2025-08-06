package com.ultraviolince.mykitchen.recipes.domain.service

import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
import kotlinx.coroutines.flow.Flow

interface AuthService {
    suspend fun login(server: String, email: String, password: String)
    suspend fun logout()
    fun getLoginState(): Flow<LoginState>
}