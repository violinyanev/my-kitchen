package com.ultraviolince.mykitchen.recipes.data.datasource.localstorage.entity

import kotlinx.serialization.Serializable
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe as SharedRecipe

@Serializable
data class Recipe(
    val title: String,
    val content: String,
    val timestamp: Long,
    val id: Long? = null
) {
    override fun toString() = "Recipe[$id] $title (ts $timestamp)"

    // Convert to shared Recipe model
    fun toSharedRecipe(): SharedRecipe {
        return SharedRecipe(
            title = title,
            content = content,
            timestamp = timestamp,
            id = id
        )
    }

    companion object {
        // Convert from shared Recipe model
        fun fromSharedRecipe(recipe: SharedRecipe): Recipe {
            return Recipe(
                title = recipe.title,
                content = recipe.content,
                timestamp = recipe.timestamp,
                id = recipe.id
            )
        }
    }
}