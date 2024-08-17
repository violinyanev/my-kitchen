package com.ultraviolince.mykitchen.recipes.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import mykitchen.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.StringResource

@Entity
class Recipe(
    val title: String,
    val content: String,
    val timestamp: Long,
    @PrimaryKey val id: Long? = null
) {
    override fun toString() = "Recipe[$id] $title (ts $timestamp)"
}
