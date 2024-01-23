package com.example.myapplication.recipes.domain.usecase

import com.example.myapplication.recipes.domain.model.Recipe
import com.example.myapplication.recipes.domain.repository.RecipeRepository

class GetRecipe(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(id: Long): Recipe? {
        return repository.getRecipeById(id)
    }
}
