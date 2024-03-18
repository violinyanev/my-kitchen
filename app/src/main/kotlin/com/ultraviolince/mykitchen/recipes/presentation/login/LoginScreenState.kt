package com.ultraviolince.mykitchen.recipes.presentation.login

import com.ultraviolince.mykitchen.recipes.domain.model.User
import com.ultraviolince.mykitchen.recipes.presentation.editrecipe.RecipeTextFieldState


data class LoginScreenState(
    val password: RecipeTextFieldState,
    val stage: LoginScreenStage,
)
