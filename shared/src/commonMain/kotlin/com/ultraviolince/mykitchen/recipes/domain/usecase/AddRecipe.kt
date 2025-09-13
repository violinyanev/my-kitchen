package com.ultraviolince.mykitchen.recipes.domain.usecase

import com.ultraviolince.mykitchen.recipes.domain.model.InvalidRecipeException
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.model.RecipeValidationError
import com.ultraviolince.mykitchen.recipes.domain.repository.RecipeRepository

class AddRecipe(private val repository: RecipeRepository) {
    suspend operator fun invoke(recipe: Recipe) {
        if (recipe.title.isBlank()) {
            throw InvalidRecipeException(RecipeValidationError.MISSING_TITLE)
        }

        if (recipe.content.isBlank()) {
            throw InvalidRecipeException(RecipeValidationError.MISSING_CONTENT)
        }
        repository.insertRecipe(recipe)
    }
}
