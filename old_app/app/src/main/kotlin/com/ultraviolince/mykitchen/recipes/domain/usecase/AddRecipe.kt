package com.ultraviolince.mykitchen.recipes.domain.usecase

import com.ultraviolince.mykitchen.R
import com.ultraviolince.mykitchen.recipes.domain.model.InvalidRecipeException
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.repository.RecipeRepository
import org.koin.core.annotation.Single

@Single
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
