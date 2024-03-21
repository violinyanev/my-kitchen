package com.ultraviolince.mykitchen.recipes.presentation.create_user

import com.ultraviolince.mykitchen.recipes.presentation.editrecipe.RecipeTextFieldState


data class CreateUserScreenState(
    val existingUser: Boolean = false,
    val server: RecipeTextFieldState,
    val username: RecipeTextFieldState,
    val email: RecipeTextFieldState,
    val buttonLoading: Boolean = false
)
