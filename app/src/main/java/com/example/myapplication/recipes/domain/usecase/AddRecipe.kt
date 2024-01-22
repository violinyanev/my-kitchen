package com.example.myapplication.recipes.domain.usecase

import com.example.myapplication.R
import com.example.myapplication.recipes.domain.model.InvalidRecipeException
import com.example.myapplication.recipes.domain.model.Recipe
import com.example.myapplication.recipes.domain.repository.RecipeRepository

class AddRecipe(private val repository: RecipeRepository) {
    suspend operator fun invoke(recipe: Recipe) {
        if (recipe.title.isBlank()) {
            throw InvalidRecipeException(R.string.missing_title)
        }

        if (recipe.content.isBlank()) {
            throw InvalidRecipeException(R.string.missing_body)
        }
        repository.insertRecipe(recipe)
    }
}
