package com.ultraviolince.mykitchen.recipes.data.datasource.backend.data

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val email: String, val password: String)
