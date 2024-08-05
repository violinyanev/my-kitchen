package com.ultraviolince.mykitchen.recipes.domain.usecase

import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.repository.RecipeRepository
import org.koin.core.annotation.Single

@Single
class GetRecipe(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(id: Long): Recipe? {
        return repository.getRecipeById(id)
    }
}
