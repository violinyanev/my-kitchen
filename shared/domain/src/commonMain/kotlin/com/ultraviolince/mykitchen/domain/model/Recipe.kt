package com.ultraviolince.mykitchen.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Recipe(
    val id: String,
    val title: String,
    val content: String,
    val timestamp: Instant,
    val synced: Boolean = false,
    val deleted: Boolean = false,
)
