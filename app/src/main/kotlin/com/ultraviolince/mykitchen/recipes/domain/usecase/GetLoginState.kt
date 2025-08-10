package com.ultraviolince.mykitchen.recipes.domain.usecase

import com.ultraviolince.mykitchen.recipes.domain.repository.AuthRepository
import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class GetLoginState(private val authRepository: AuthRepository) {
    operator fun invoke(): Flow<LoginState> {
        return authRepository.getLoginState()
    }
}
