package com.ultraviolince.mykitchen.recipes.domain.usecase

import com.ultraviolince.mykitchen.recipes.domain.repository.AuthRepository
import org.koin.core.annotation.Single

@Single
class Login(private val authRepository: AuthRepository) {
    suspend operator fun invoke(server: String, username: String, password: String) {
        authRepository.login(server, username, password)
    }
}
