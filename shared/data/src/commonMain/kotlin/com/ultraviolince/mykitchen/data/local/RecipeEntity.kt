package com.ultraviolince.mykitchen.data.local

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val timestamp: Long,
    val synced: Boolean,
    val deleted: Boolean,
)
