package com.ultraviolince.mykitchen.recipes.domain.usecase

import com.ultraviolince.mykitchen.recipes.domain.repository.AuthRepository

class Logout(private val authRepository: AuthRepository) {
    suspend operator fun invoke() {
        authRepository.logout()
    }
}
