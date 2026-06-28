package com.ultraviolince.mykitchen.domain.usecase

import com.ultraviolince.mykitchen.domain.model.AuthState
import com.ultraviolince.mykitchen.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow

class GetAuthStateUseCase(private val repository: RecipeRepository) {
    operator fun invoke(): Flow<AuthState> = repository.getAuthState()
}
