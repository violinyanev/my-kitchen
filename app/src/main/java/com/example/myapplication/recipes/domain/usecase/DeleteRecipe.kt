package com.example.myapplication.recipes.domain.usecase

import com.example.myapplication.recipes.domain.model.Recipe
import com.example.myapplication.recipes.domain.repository.RecipeRepository

class DeleteRecipe(private val repository: RecipeRepository) {
    suspend operator fun invoke(recipe: Recipe) {
        repository.deleteRecipe(recipe)
    }
}
