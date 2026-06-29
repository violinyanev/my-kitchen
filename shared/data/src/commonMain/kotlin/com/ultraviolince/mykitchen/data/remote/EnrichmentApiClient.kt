package com.ultraviolince.mykitchen.data.remote

import com.ultraviolince.mykitchen.data.remote.dto.EnrichmentDto
import com.ultraviolince.mykitchen.data.remote.dto.RefineRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

class EnrichmentApiClient(private val httpClient: HttpClient) {

    suspend fun beautify(serverUrl: String, token: String, recipeId: String): Result<EnrichmentDto> =
        runCatching {
            httpClient.post("$serverUrl/recipes/$recipeId/enrichment/beautify") {
                bearerAuth(token)
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
