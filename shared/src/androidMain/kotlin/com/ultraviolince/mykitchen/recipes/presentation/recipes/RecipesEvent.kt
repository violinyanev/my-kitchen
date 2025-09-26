package com.ultraviolince.mykitchen.recipes.presentation.recipes

import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.util.RecipeOrder

sealed class RecipesEvent {
    data class Order(val recipesOrder: RecipeOrder) : RecipesEvent()
    data class DeleteRecipe(val recipe: Recipe) : RecipesEvent()

    object RestoreRecipe : RecipesEvent()
    object ToggleOrderSection : RecipesEvent()
}
