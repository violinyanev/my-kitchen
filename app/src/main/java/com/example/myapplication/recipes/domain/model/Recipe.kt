package com.example.myapplication.recipes.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.ui.theme.md_theme_light_surface

@Entity
class Recipe(
    val title: String,
    val content: String,
    val timestamp: Long,
    @PrimaryKey val id: Int? = null
) {
}

class InvalidRecipeException(message: String) : Exception(message)
