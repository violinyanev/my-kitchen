package com.ultraviolince.mykitchen.server.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String,
)

@Serializable
data class RegisterRequestDto(
    val email: String,
    val password: String,
)

@Serializable
data class LoginResponseDto(
    val token: String,
)

@Serializable
data class RecipeResponseDto(
    val id: String,
    val title: String,
    val content: String,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("updated_at") val updatedAt: Long,
)

@Serializable
data class CreateRecipeRequestDto(
    val title: String,
    val content: String,
    val id: String? = null,
    @SerialName("created_at") val createdAt: Long? = null,
    @SerialName("updated_at") val updatedAt: Long? = null,
)

@Serializable
data class ErrorDto(val error: String)

@Serializable
data class VersionResponse(val version: String)
