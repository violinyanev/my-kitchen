package com.ultraviolince.mykitchen.data.remote

import com.ultraviolince.mykitchen.data.remote.dto.EnrichmentDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode

/**
 * Enrichments are generated server-side by a background worker; the client
 * only reads them to show the beautified version of a recipe.
 */
class EnrichmentApiClient(private val httpClient: HttpClient) {

    suspend fun getEnrichment(serverUrl: String, token: String, recipeId: String): Result<EnrichmentDto?> =
        runCatching {
            val response = httpClient.get("$serverUrl/recipes/$recipeId/enrichment") {
                bearerAuth(token)
            }
            if (response.status == HttpStatusCode.NotFound) null else response.body<EnrichmentDto>()
        }
}
