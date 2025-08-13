package com.ultraviolince.mykitchen.recipes.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Recipe(
    val title: String,
    val content: String,
    val timestamp: Long,
    val id: Long? = null
) {
    override fun toString() = "Recipe[$id] $title (ts $timestamp)"
}

class InvalidRecipeException(val errorMessage: String) : Exception(errorMessage)