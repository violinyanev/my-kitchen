package com.ultraviolince.mykitchen.domain.repository

import com.ultraviolince.mykitchen.domain.model.RecipeEnrichment

interface EnrichmentRepository {
    suspend fun beautify(recipeId: String): Result<RecipeEnrichment>
    suspend fun getEnrichment(recipeId: String): Result<RecipeEnrichment?>
    suspend fun refine(recipeId: String, feedback: String): Result<RecipeEnrichment>
    suspend fun deleteEnrichment(recipeId: String): Result<Unit>
}
