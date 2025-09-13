package com.ultraviolince.mykitchen.recipes.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(server: String, email: String, password: String)
    suspend fun logout()
    fun getLoginState(): Flow<LoginState>
}
