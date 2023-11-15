package com.example.myapplication.recipes.domain.usecase

import com.example.myapplication.recipes.domain.model.InvalidRecipeException
import com.example.myapplication.recipes.domain.model.Recipe
import com.example.myapplication.recipes.domain.repository.RecipeRepository

class AddRecipe(private val repository: RecipeRepository) {
    suspend operator fun invoke(recipe: Recipe) {
        if (recipe.title.isBlank()) {
            throw InvalidRecipeException("The title of the recipe can't be empty")
        }

        if (recipe.content.isBlank()) {
            throw InvalidRecipeException("The body of the recipe can't be empty")
        }
        repository.insertRecipe(recipe)
    }
}
