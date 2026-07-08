package com.ultraviolince.mykitchen.data.remote

import com.ultraviolince.mykitchen.data.remote.dto.EnrichmentDto
import com.ultraviolince.mykitchen.data.remote.dto.RefineRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

class EnrichmentApiClient(private val httpClient: HttpClient) {

    // Enrichment runs a local LLM (CPU inference), which can take minutes.
    // The server processes one generation at a time, so a request may also
    // queue behind another one; 10 min covers queue wait + generation.
    private val enrichmentTimeoutMillis = 600_000L

    suspend fun beautify(serverUrl: String, token: String, recipeId: String): Result<EnrichmentDto> =
        runCatching {
            httpClient.post("$serverUrl/recipes/$recipeId/enrichment/beautify") {
                bearerAuth(token)
                timeout { requestTimeoutMillis = enrichmentTimeoutMillis }
            }.body<EnrichmentDto>()
        }

    suspend fun getEnrichment(serverUrl: String, token: String, recipeId: String): Result<EnrichmentDto?> =
        runCatching {
            val response = httpClient.get("$serverUrl/recipes/$recipeId/enrichment") {
                bearerAuth(token)
            }
            if (response.status == HttpStatusCode.NotFound) null else response.body<EnrichmentDto>()
        }

    suspend fun refine(
        serverUrl: String,
        token: String,
        recipeId: String,
        feedback: String,
    ): Result<EnrichmentDto> =
        runCatching {
            httpClient.post("$serverUrl/recipes/$recipeId/enrichment/refine") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                timeout { requestTimeoutMillis = enrichmentTimeoutMillis }
                setBody(RefineRequestDto(feedback))
            }.body<EnrichmentDto>()
        }

    suspend fun deleteEnrichment(serverUrl: String, token: String, recipeId: String): Result<Unit> =
        runCatching {
            httpClient.delete("$serverUrl/recipes/$recipeId/enrichment") {
                bearerAuth(token)
            }
            Unit
        }
}
