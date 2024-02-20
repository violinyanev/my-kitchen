package com.ultraviolince.mykitchen.recipes.data.datasource.backend

data class LoginResultData(
    val username: String,
    val token: String
)

data class LoginResult(val data: LoginResultData)
