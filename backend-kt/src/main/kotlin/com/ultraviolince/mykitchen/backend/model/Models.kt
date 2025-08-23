package com.ultraviolince.mykitchen.backend.model

import kotlinx.serialization.Serializable

@Serializable
data class Recipe(
    val id: Int,
    val title: String,
    val body: String = "",
    val timestamp: Long,
    val user: String
)

@Serializable
data class RecipeRequest(
    val id: Int? = null,
    val title: String,
    val body: String = "",
    val timestamp: Long? = null
)

@Serializable
data class RecipeResponse(
    val message: String,
    val recipe: Recipe
)

@Serializable
data class User(
    val name: String,
    val email: String,
    val password: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val message: String,
    val data: UserData
)

@Serializable
data class UserData(
    val email: String,
    val username: String,
    val token: String
)

@Serializable
data class ApiVersion(
    val api_version_major: Int,
    val api_version_minor: Int,
    val api_version_patch: Int
)

@Serializable
data class ErrorResponse(
    val message: String,
    val data: String? = null,
    val error: String
)