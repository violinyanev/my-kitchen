package com.ultraviolince.mykitchen.data.repository

import com.ultraviolince.mykitchen.data.remote.EnrichmentApiClient
import com.ultraviolince.mykitchen.data.remote.dto.toDomain
import com.ultraviolince.mykitchen.data.store.CredentialsStore
import com.ultraviolince.mykitchen.domain.model.RecipeEnrichment
import com.ultraviolince.mykitchen.domain.repository.EnrichmentRepository

class EnrichmentRepositoryImpl(
    private val api: EnrichmentApiClient,
    private val credentials: CredentialsStore,
) : EnrichmentRepository {

    override suspend fun getEnrichment(recipeId: String): Result<RecipeEnrichment?> {
        val token = credentials.getToken()
            ?: return Result.failure(IllegalStateException("Not logged in"))
        val serverUrl = credentials.getServerUrl()
            ?: return Result.failure(IllegalStateException("Not logged in"))
        return api.getEnrichment(serverUrl, token, recipeId).map { it?.toDomain() }
    }
}
