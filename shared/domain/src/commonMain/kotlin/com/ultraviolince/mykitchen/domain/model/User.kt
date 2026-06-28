package com.ultraviolince.mykitchen.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val email: String,
    val serverUrl: String,
    val token: String? = null,
)

sealed class AuthState {
    data object LoggedOut : AuthState()
    data class LoggedIn(val user: User) : AuthState()
}
