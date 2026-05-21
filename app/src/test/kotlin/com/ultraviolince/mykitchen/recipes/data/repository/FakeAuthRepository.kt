package com.ultraviolince.mykitchen.recipes.data.repository

import com.ultraviolince.mykitchen.recipes.domain.repository.AuthRepository
import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeAuthRepository : AuthRepository {

    override suspend fun login(server: String, email: String, password: String) {
        // Fake implementation for tests
    }

    override suspend fun logout() {
        // Fake implementation for tests
    }

    override fun getLoginState(): Flow<LoginState> {
        return flow {
            emit(LoginState.LoginEmpty)
        }
    }
}
