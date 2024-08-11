package com.ultraviolince.mykitchen.recipes.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Recipe(
    val title: String,
    val content: String,
    val timestamp: Long,
    @PrimaryKey(autoGenerate = true) val id: Long = 0L
) {
    override fun toString() = "Recipe[$id] $title (ts $timestamp)"
}
