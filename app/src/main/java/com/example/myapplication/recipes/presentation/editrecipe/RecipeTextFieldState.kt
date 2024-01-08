package com.example.myapplication.recipes.presentation.editrecipe

import androidx.core.content.res.ResourcesCompat.ID_NULL

data class RecipeTextFieldState(
    val text: String = "",
    val hintStringId: Int = ID_NULL,
    val isHintVisible: Boolean = false
)
