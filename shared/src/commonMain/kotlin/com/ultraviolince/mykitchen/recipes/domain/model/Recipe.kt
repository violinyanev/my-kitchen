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

enum class RecipeValidationError {
    MISSING_TITLE,
    MISSING_CONTENT
}

class InvalidRecipeException(val error: RecipeValidationError) : Exception("Recipe validation failed: $error")