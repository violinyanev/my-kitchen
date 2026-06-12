package com.ultraviolince.mykitchen.domain.model

sealed class RecipeOrder(val ascending: Boolean) {
    class Title(ascending: Boolean = true) : RecipeOrder(ascending)
    class Date(ascending: Boolean = false) : RecipeOrder(ascending)
}
