package com.ultraviolince.mykitchen.domain.repository

import com.ultraviolince.mykitchen.domain.model.RecipeEnrichment

interface EnrichmentRepository {
    suspend fun getEnrichment(recipeId: String): Result<RecipeEnrichment?>
    suspend fun getEnrichments(): Result<List<RecipeEnrichment>>
}
