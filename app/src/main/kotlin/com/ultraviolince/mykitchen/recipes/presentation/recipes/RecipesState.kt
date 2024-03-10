package com.ultraviolince.mykitchen.recipes.presentation.recipes

import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.repository.CloudSyncState
import com.ultraviolince.mykitchen.recipes.domain.util.OrderType
import com.ultraviolince.mykitchen.recipes.domain.util.RecipeOrder

data class RecipesState(
    val recipes: List<Recipe> = emptyList(),
    val recipeOrder: RecipeOrder = RecipeOrder.Date(OrderType.Descending),
    val isOrderSelectionVisible: Boolean = false,
    val syncState: CloudSyncState = CloudSyncState.NotLoggedIn
)
