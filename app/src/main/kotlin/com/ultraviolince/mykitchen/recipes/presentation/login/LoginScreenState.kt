package com.ultraviolince.mykitchen.recipes.presentation.login

import com.ultraviolince.mykitchen.recipes.presentation.editrecipe.RecipeTextFieldState


data class LoginScreenState(
    val server: RecipeTextFieldState,
    val username: RecipeTextFieldState,
    val email: RecipeTextFieldState,
    val password: RecipeTextFieldState,
    val stage: LoginScreenStage,
    val buttonLoading: Boolean = false
)
