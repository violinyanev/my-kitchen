package com.ultraviolince.mykitchen.recipes.presentation.login

import com.ultraviolince.mykitchen.recipes.domain.model.User

sealed class LoginScreenStage {
    data object Loading : LoginScreenStage()
    data class EnterPassword(val user: User) : LoginScreenStage()
    data class AwaitServerResponse(val user: User) : LoginScreenStage()
}
