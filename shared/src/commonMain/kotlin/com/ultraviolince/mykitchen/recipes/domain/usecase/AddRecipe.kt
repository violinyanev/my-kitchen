package com.ultraviolince.mykitchen.recipes.domain.usecase

import com.ultraviolince.mykitchen.recipes.domain.model.InvalidRecipeException
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.repository.RecipeRepository

class AddRecipe(private val repository: RecipeRepository) {
    suspend operator fun invoke(recipe: Recipe) {
        if (recipe.title.isBlank()) {
            throw InvalidRecipeException("Recipe title cannot be empty")
        }

        if (recipe.content.isBlank()) {
            throw InvalidRecipeException("Recipe content cannot be empty")
        }
        repository.insertRecipe(recipe)
    }
}
