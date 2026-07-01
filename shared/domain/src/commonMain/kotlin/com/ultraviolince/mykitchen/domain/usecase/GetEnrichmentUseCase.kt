package com.ultraviolince.mykitchen.domain.usecase

import com.ultraviolince.mykitchen.domain.repository.EnrichmentRepository

class GetEnrichmentUseCase(private val repository: EnrichmentRepository) {
    suspend operator fun invoke(recipeId: String) = repository.getEnrichment(recipeId)
}
