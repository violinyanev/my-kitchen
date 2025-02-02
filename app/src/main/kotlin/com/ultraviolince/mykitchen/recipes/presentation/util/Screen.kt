package com.ultraviolince.mykitchen.recipes.presentation.util

import kotlinx.serialization.Serializable

@Serializable
object LoginScreenTarget

@Serializable
object RecipesScreenTarget

@Serializable
data class AddEditRecipeScreenTarget(
    val recipeId: Long?
)
