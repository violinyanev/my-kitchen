package com.ultraviolince.mykitchen.domain.usecase

import com.ultraviolince.mykitchen.domain.model.Recipe
import com.ultraviolince.mykitchen.domain.repository.RecipeRepository

class AddRecipeUseCase(private val repository: RecipeRepository) {
    suspend operator fun invoke(title: String, content: String): Result<Unit> {
        if (title.isBlank()) return Result.failure(IllegalArgumentException("Title cannot be blank"))
        if (content.isBlank()) return Result.failure(IllegalArgumentException("Content cannot be blank"))
        repository.insertRecipe(Recipe.create(title.trim(), content.trim()))
        return Result.success(Unit)
    }
}
