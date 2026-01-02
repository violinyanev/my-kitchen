package com.ultraviolince.mykitchen.recipes.domain.usecase

import com.ultraviolince.mykitchen.recipes.domain.repository.AuthRepository

class Login(private val authRepository: AuthRepository) {
    suspend operator fun invoke(server: String, username: String, password: String) {
        authRepository.login(server, username, password)
    }
}
