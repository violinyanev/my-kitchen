package com.example.myapplication.recipes.presentation.notes.components

data class RecipeTextFieldState(
    val text: String = "",
    val hint: String = "",
    val isHintVisible: Boolean = false
)
