package com.ultraviolince.mykitchen.recipes.data.datasource.backend.data

import kotlinx.serialization.Serializable

@Serializable
data class RecipeList(
    val result: List<BackendRecipe>
)