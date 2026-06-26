package com.ultraviolince.mykitchen.domain.usecase

import com.ultraviolince.mykitchen.domain.repository.RecipeRepository

class LoginUseCase(private val repository: RecipeRepository) {
    suspend operator fun invoke(email: String, password: String, serverUrl: String): Result<Unit> {
        if (email.isBlank()) return Result.failure(IllegalArgumentException("Email cannot be blank"))
        if (serverUrl.isBlank()) return Result.failure(IllegalArgumentException("Server URL cannot be blank"))
        return repository.login(email, password, serverUrl)
    }
}
