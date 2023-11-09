package com.example.myapplication.recipes.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.ui.theme.Purple80

@Entity
class Recipe(
    val title: String,
    val content: String,
    val timestamp: Long,
    val color: Int,
    @PrimaryKey val id: Int? = null
) {
    companion object {
        val recipeColors = listOf(Purple80)
    }
}
