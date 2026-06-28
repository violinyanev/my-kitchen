package com.ultraviolince.mykitchen.domain.usecase

import com.ultraviolince.mykitchen.domain.repository.RecipeRepository

class SyncRecipesUseCase(private val repository: RecipeRepository) {
    suspend operator fun invoke(): Result<Unit> = repository.syncRecipes()
}
