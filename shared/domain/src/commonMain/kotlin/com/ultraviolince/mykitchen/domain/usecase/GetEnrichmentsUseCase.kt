package com.ultraviolince.mykitchen.domain.usecase

import com.ultraviolince.mykitchen.domain.repository.EnrichmentRepository

class GetEnrichmentsUseCase(private val repository: EnrichmentRepository) {
    suspend operator fun invoke() = repository.getEnrichments()
}
