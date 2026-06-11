package com.ultraviolince.mykitchen.recipes.presentation.recipes

import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
import com.ultraviolince.mykitchen.recipes.domain.util.OrderType
import com.ultraviolince.mykitchen.recipes.domain.util.RecipeOrder
import com.ultraviolince.mykitchen.recipes.presentation.util.UiState

data class RecipesState(
    val recipes: ImmutableRecipesList = ImmutableRecipesList(emptyList()),
    val recipeOrder: RecipeOrder = RecipeOrder.Date(OrderType.Descending),
    val isOrderSelectionVisible: Boolean = false,
    val syncState: LoginState = LoginState.LoginEmpty
) : UiState
