package com.ultraviolince.mykitchen.recipes.domain.usecase

import com.ultraviolince.mykitchen.recipes.domain.model.InvalidRecipeException
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.repository.RecipeRepository
import mykitchen.composeapp.generated.resources.Res
import mykitchen.composeapp.generated.resources.missing_body
import mykitchen.composeapp.generated.resources.missing_title

// TODO kmp @Single
class AddRecipe(private val repository: RecipeRepository) {
    suspend operator fun invoke(recipe: Recipe) {
        if (recipe.title.isBlank()) {
            throw InvalidRecipeException(Res.string.missing_title)
        }

        if (recipe.content.isBlank()) {
            throw InvalidRecipeException(Res.string.missing_body)
        }
        repository.insertRecipe(recipe)
    }
}
