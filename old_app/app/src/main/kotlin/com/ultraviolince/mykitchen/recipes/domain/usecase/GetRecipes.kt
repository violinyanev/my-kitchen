package com.ultraviolince.mykitchen.recipes.domain.usecase

import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.repository.RecipeRepository
import com.ultraviolince.mykitchen.recipes.domain.util.OrderType
import com.ultraviolince.mykitchen.recipes.domain.util.RecipeOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
class GetRecipes(private val repository: RecipeRepository) {
    operator fun invoke(recipeOrder: RecipeOrder = RecipeOrder.Date(OrderType.Descending)): Flow<List<Recipe>> {
        return repository.getRecipes().map { recipes ->
            when (recipeOrder.orderType) {
                is OrderType.Ascending -> {
                    when (recipeOrder) {
                        is RecipeOrder.Title -> recipes.sortedBy { it.title.lowercase() }
                        is RecipeOrder.Date -> recipes.sortedBy { it.timestamp }
                    }
                }
                is OrderType.Descending -> {
                    when (recipeOrder) {
                        is RecipeOrder.Title -> recipes.sortedByDescending { it.title.lowercase() }
                        is RecipeOrder.Date -> recipes.sortedByDescending { it.timestamp }
                    }
                }
            }
        }
    }
}
