package com.ultraviolince.mykitchen.recipes.data.datasource.localdb.entity

import androidx.annotation.StringRes
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ultraviolince.mykitchen.recipes.domain.model.SyncStatus
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe as SharedRecipe

@Entity
data class Recipe(
    val title: String,
    val content: String,
    val timestamp: Long,
    val syncStatus: SyncStatus = SyncStatus.NOT_SYNCED,
    val lastSyncTimestamp: Long? = null,
    val syncErrorMessage: String? = null,
    @PrimaryKey val id: Long? = null
) {
    override fun toString() = "Recipe[$id] $title (ts $timestamp, sync: $syncStatus)"

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

class InvalidRecipeException(@param:StringRes val errorString: Int) : Exception()
