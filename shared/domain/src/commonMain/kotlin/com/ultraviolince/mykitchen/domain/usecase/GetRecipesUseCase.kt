package com.ultraviolince.mykitchen.domain.usecase

import com.ultraviolince.mykitchen.domain.model.Recipe
import com.ultraviolince.mykitchen.domain.model.RecipeOrder
import com.ultraviolince.mykitchen.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow

class GetRecipesUseCase(private val repository: RecipeRepository) {
    operator fun invoke(order: RecipeOrder = RecipeOrder.Date()): Flow<List<Recipe>> =
        repository.getRecipes(order)
}
