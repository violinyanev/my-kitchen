package com.ultraviolince.mykitchen.recipes.domain.model

import androidx.annotation.StringRes
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Recipe(
    val title: String,
    val content: String,
    val timestamp: Long,
    @PrimaryKey val id: Long? = null
) {
    override fun toString() = "Recipe[$id] $title (ts $timestamp)"
}

class InvalidRecipeException(@StringRes val errorString: Int) : Exception()
