package com.ultraviolince.mykitchen.recipes.data.datasource.backend.data

import kotlinx.serialization.Serializable

@Serializable
data class BackendRecipeResponse(val recipe: BackendRecipe)
