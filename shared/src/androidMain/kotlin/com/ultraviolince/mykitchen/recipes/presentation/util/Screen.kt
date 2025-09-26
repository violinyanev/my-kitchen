package com.ultraviolince.mykitchen.recipes.presentation.util

import kotlinx.serialization.Serializable

@Serializable
object LoginScreen

@Serializable
object RecipesScreen

@Serializable
data class AddEditRecipeScreen(val recipeId: Int = -1)
