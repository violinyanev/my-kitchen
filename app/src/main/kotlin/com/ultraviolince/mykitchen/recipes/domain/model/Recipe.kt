package com.ultraviolince.mykitchen.recipes.domain.model

import androidx.annotation.StringRes
import androidx.room.Entity
import androidx.room.PrimaryKey

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
}

class InvalidRecipeException(@param:StringRes val errorString: Int) : Exception()
