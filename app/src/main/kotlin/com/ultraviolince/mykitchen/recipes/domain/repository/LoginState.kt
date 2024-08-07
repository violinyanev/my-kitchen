package com.ultraviolince.mykitchen.recipes.domain.repository

import com.ultraviolince.mykitchen.recipes.data.datasource.backend.util.NetworkError

sealed class LoginState {
    data object LoginEmpty : LoginState()
    data object LoginPending : LoginState()
    data object LoginSuccess : LoginState()
    data class LoginFailure(val error: NetworkError) : LoginState()
}
