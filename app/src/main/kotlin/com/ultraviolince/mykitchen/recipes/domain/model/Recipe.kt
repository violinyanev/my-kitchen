package com.ultraviolince.mykitchen.recipes.domain.model

import androidx.annotation.StringRes
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Recipe(
    val title: String,
    val content: String,
    val timestamp: Long,
    @PrimaryKey val id: Long? = null
) {
    override fun toString() = "Recipe[$id] $title (ts $timestamp)"

    // Convert to shared Recipe model
    fun toSharedRecipe(): com.ultraviolince.mykitchen.recipes.domain.model.Recipe {
        return com.ultraviolince.mykitchen.recipes.domain.model.Recipe(
            title = title,
            content = content,
            timestamp = timestamp,
            id = id
        )
    }

    companion object {
        // Convert from shared Recipe model
        fun fromSharedRecipe(recipe: com.ultraviolince.mykitchen.recipes.domain.model.Recipe): Recipe {
            return Recipe(
                title = recipe.title,
                content = recipe.content,
                timestamp = recipe.timestamp,
                id = recipe.id
            )
        }
    }
}

class InvalidRecipeException(@param:StringRes val errorString: Int) : Exception()
