package com.example.myapplication.recipes.domain.repository

sealed class LoginState {
    data object LoginEmpty : LoginState()
    data object LoginPending : LoginState()
    data object LoginSuccess : LoginState()
    data object LoginFailure : LoginState()
}
