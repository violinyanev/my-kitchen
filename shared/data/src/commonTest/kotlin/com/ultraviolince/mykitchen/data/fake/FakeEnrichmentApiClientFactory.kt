package com.ultraviolince.mykitchen.data.fake

import com.ultraviolince.mykitchen.data.remote.EnrichmentApiClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** Creates an EnrichmentApiClient backed by a configurable MockEngine. */
fun buildMockEnrichmentApiClient(
    succeeds: Boolean = true,
    enrichmentJson: String = """
        {"id":"enr-1","recipe_id":"recipe-1","image_url":null,"image_credit":null,
         "tags":["quick"],"links":[],"summary":"Tasty","updated_at":1000}
    """.trimIndent(),
): EnrichmentApiClient {
    val engine = MockEngine { _ ->
        if (succeeds) {
            respond(
                content = enrichmentJson,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        } else {
            respond(content = """{"error":"unavailable"}""", status = HttpStatusCode.ServiceUnavailable)
        }
    }
    val client = HttpClient(engine) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }
    return EnrichmentApiClient(client)
}
