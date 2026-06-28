package com.ultraviolince.mykitchen.domain.usecase

import com.ultraviolince.mykitchen.domain.repository.RecipeRepository

class LogoutUseCase(private val repository: RecipeRepository) {
    suspend operator fun invoke() = repository.logout()
}
