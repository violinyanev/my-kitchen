package com.ultraviolince.mykitchen.data.remote.dto

import com.ultraviolince.mykitchen.domain.model.RecipeEnrichment
import com.ultraviolince.mykitchen.domain.model.RecipeLink
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EnrichmentDto(
    val id: String,
    @SerialName("recipe_id") val recipeId: String,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("image_credit") val imageCredit: String? = null,
    val tags: List<String> = emptyList(),
    val links: List<RecipeLinkDto> = emptyList(),
    val summary: String = "",
    @SerialName("updated_at") val updatedAt: Long = 0,
)

@Serializable
data class RecipeLinkDto(
    val title: String,
    val url: String,
    val description: String,
)

@Serializable
data class RefineRequestDto(val feedback: String)

fun EnrichmentDto.toDomain() = RecipeEnrichment(
    id = id,
    recipeId = recipeId,
    imageUrl = imageUrl,
    imageCredit = imageCredit,
    tags = tags,
    links = links.map { RecipeLink(it.title, it.url, it.description) },
    summary = summary,
    updatedAt = updatedAt,
)
