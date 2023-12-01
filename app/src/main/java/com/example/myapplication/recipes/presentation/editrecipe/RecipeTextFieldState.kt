package com.example.myapplication.recipes.presentation.editrecipe

data class RecipeTextFieldState(
    val text: String = "",
    val hint: String = "",
    val isHintVisible: Boolean = false
)
