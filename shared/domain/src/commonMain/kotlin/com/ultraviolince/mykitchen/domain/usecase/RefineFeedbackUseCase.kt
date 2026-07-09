package com.ultraviolince.mykitchen.domain.usecase

import com.ultraviolince.mykitchen.domain.repository.EnrichmentRepository

class RefineFeedbackUseCase(private val repository: EnrichmentRepository) {
    suspend operator fun invoke(recipeId: String, feedback: String) =
        repository.refine(recipeId, feedback)
}
