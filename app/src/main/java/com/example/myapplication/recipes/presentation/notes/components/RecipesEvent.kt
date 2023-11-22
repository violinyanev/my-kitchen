package com.example.myapplication.recipes.presentation.notes.components

import com.example.myapplication.recipes.domain.model.Recipe
import com.example.myapplication.recipes.domain.util.RecipeOrder

sealed class RecipesEvent {
    data class Order(val recipesOrder: RecipeOrder) : RecipesEvent()
    data class DeleteRecipe(val recipe: Recipe) : RecipesEvent()

    object RestoreRecipe : RecipesEvent()
    object ToggleOrderSection : RecipesEvent()
}
