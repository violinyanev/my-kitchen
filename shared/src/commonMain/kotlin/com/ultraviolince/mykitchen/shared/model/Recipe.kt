package com.ultraviolince.mykitchen.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class Recipe(
    val id: Long? = null,
    val title: String,
    val ingredients: List<String>,
    val content: String,
    val createdAtEpochMillis: Long
)

