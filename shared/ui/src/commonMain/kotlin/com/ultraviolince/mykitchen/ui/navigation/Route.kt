package com.ultraviolince.mykitchen.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Route {
    @Serializable
    data object RecipeList : Route()

    @Serializable
    data class EditRecipe(val id: String? = null) : Route()

    @Serializable
    data class BeautifyRecipe(val id: String) : Route()

    @Serializable
    data object Login : Route()
}
