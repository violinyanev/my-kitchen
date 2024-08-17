package com.ultraviolince.mykitchen.recipes.domain.usecase

import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.repository.RecipeRepository

// TODO kmp @Single
class DeleteRecipe(private val repository: RecipeRepository) {
    suspend operator fun invoke(recipe: Recipe) {
        repository.deleteRecipe(recipe)
    }
}
