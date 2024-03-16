package com.ultraviolince.mykitchen.recipes.presentation.login

enum class LoginScreenStage {
    LOADING,
    CREATE_USER,
    ENTER_PASSWORD
}

/*sealed class LoginScreenStage {
    data object Loading : LoginScreenStage()
    data object Prompt : LoginScreenStage()
}*/
