package com.ultraviolince.mykitchen.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EnrichmentApiClientTest {

    private fun buildClient(mockEngine: MockEngine): HttpClient = HttpClient(mockEngine) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    private val enrichmentJson = """
        {"id":"enr-1","recipe_id":"recipe-1","image_url":"http://img","image_credit":"Jane",
         "tags":["quick"],"links":[{"title":"t","url":"http://u","description":"d"}],
         "summary":"Tasty","updated_at":1000}
    """.trimIndent()

    @Test
    fun getEnrichmentReturnsNullOn404() = runTest {
        val engine = MockEngine { _ ->
            respond(content = """{"error":"No enrichment found"}""", status = HttpStatusCode.NotFound)
        }
        val client = EnrichmentApiClient(buildClient(engine))
        val result = client.getEnrichment("http://localhost:5000", "tok", "recipe-1")
        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    @Test
    fun getEnrichmentReturnsEnrichmentOnSuccess() = runTest {
        val engine = MockEngine { _ ->
            respond(
                content = enrichmentJson,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val client = EnrichmentApiClient(buildClient(engine))
        val result = client.getEnrichment("http://localhost:5000", "tok", "recipe-1")
        assertTrue(result.isSuccess)
        assertEquals("enr-1", result.getOrNull()?.id)
        assertEquals("Tasty", result.getOrNull()?.summary)
    }

    @Test
    fun getEnrichmentsReturnsListOnSuccess() = runTest {
        val engine = MockEngine { _ ->
            respond(
                content = "[$enrichmentJson]",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val client = EnrichmentApiClient(buildClient(engine))
        val result = client.getEnrichments("http://localhost:5000", "tok")
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
    }

    @Test
    fun getEnrichmentsReturnsFailureOnServerError() = runTest {
        val engine = MockEngine { _ ->
            respond(content = """{"error":"boom"}""", status = HttpStatusCode.InternalServerError)
        }
        val client = EnrichmentApiClient(buildClient(engine))
        val result = client.getEnrichments("http://localhost:5000", "tok")
        assertTrue(result.isFailure)
    }

    @Test
    fun getEnrichmentReturnsFailureOnServerError() = runTest {
        val engine = MockEngine { _ ->
            respond(content = """{"error":"boom"}""", status = HttpStatusCode.InternalServerError)
        }
        val client = EnrichmentApiClient(buildClient(engine))
        val result = client.getEnrichment("http://localhost:5000", "tok", "recipe-1")
        assertTrue(result.isFailure)
    }
}
