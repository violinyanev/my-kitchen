package com.ultraviolince.mykitchen.recipes.presentation.login

enum class LoginScreenStage {
    LOADING,
    ENTER_PASSWORD,
    WAITING
}

/*sealed class LoginScreenStage {
    data object Loading : LoginScreenStage()
    data object Prompt : LoginScreenStage()
}*/
