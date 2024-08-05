package com.ultraviolince.mykitchen.recipes.data.datasource.backend.data

import kotlinx.serialization.Serializable

@Serializable
data class LoginResultData(
    val username: String,
    val token: String
)

@Serializable
data class LoginResult(val data: LoginResultData)
