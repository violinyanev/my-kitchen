package com.ultraviolince.mykitchen.domain.usecase

import com.ultraviolince.mykitchen.domain.repository.EnrichmentRepository

class BeautifyRecipeUseCase(private val repository: EnrichmentRepository) {
    suspend operator fun invoke(recipeId: String) = repository.beautify(recipeId)
}
