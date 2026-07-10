package com.ultraviolince.mykitchen.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class RecipeEnrichment(
    val id: String,
    val recipeId: String,
    val imageUrl: String?,
    val imageCredit: String?,
    val tags: List<String>,
    val links: List<RecipeLink>,
    val summary: String,
    val updatedAt: Long,
)

@Serializable
data class RecipeLink(
    val title: String,
    val url: String,
    val description: String,
)
