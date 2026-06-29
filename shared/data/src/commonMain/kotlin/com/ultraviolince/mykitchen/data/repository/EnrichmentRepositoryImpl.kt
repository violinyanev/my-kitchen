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

    override suspend fun beautify(recipeId: String): Result<RecipeEnrichment> {
        val (token, serverUrl) = credentialsOrFailure() ?: return notLoggedIn()
        return api.beautify(serverUrl, token, recipeId).map { it.toDomain() }
    }

    override suspend fun getEnrichment(recipeId: String): Result<RecipeEnrichment?> {
        val (token, serverUrl) = credentialsOrFailure() ?: return notLoggedIn()
        return api.getEnrichment(serverUrl, token, recipeId).map { it?.toDomain() }
    }

    override suspend fun refine(recipeId: String, feedback: String): Result<RecipeEnrichment> {
        val (token, serverUrl) = credentialsOrFailure() ?: return notLoggedIn()
        return api.refine(serverUrl, token, recipeId, feedback).map { it.toDomain() }
    }

    override suspend fun deleteEnrichment(recipeId: String): Result<Unit> {
        val (token, serverUrl) = credentialsOrFailure() ?: return notLoggedIn()
        return api.deleteEnrichment(serverUrl, token, recipeId)
    }

    private suspend fun credentialsOrFailure(): Pair<String, String>? {
        val token = credentials.getToken() ?: return null
        val serverUrl = credentials.getServerUrl() ?: return null
        return token to serverUrl
    }

    private fun <T> notLoggedIn(): Result<T> =
        Result.failure(IllegalStateException("Not logged in"))
}
