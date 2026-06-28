package com.ultraviolince.mykitchen.domain.usecase

import com.ultraviolince.mykitchen.domain.model.Recipe
import com.ultraviolince.mykitchen.domain.repository.RecipeRepository

class GetRecipeUseCase(private val repository: RecipeRepository) {
    suspend operator fun invoke(id: String): Recipe? = repository.getRecipeById(id)
}
