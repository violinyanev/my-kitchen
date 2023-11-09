package com.example.myapplication.recipes.domain.usecase

import com.example.myapplication.recipes.domain.model.Recipe
import com.example.myapplication.recipes.domain.repository.RecipeRepository
import com.example.myapplication.recipes.domain.util.OrderType
import com.example.myapplication.recipes.domain.util.RecipeOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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
