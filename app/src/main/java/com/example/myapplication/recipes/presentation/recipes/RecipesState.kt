package com.example.myapplication.recipes.presentation.recipes

import com.example.myapplication.recipes.domain.model.Recipe
import com.example.myapplication.recipes.domain.util.OrderType
import com.example.myapplication.recipes.domain.util.RecipeOrder

data class RecipesState(
    val recipes: List<Recipe> = emptyList(),
    val recipeOrder: RecipeOrder = RecipeOrder.Date(OrderType.Descending),
    val isOrderSelectionVisible: Boolean = false
)
