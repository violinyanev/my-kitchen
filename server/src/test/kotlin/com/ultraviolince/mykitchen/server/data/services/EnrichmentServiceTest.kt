package com.ultraviolince.mykitchen.server.data.services

import com.ultraviolince.mykitchen.server.config.AppConfig
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class EnrichmentServiceTest {

    private fun configFor(baseUrl: String) = AppConfig(
        jwtSecret = "test-secret",
        jwtIssuer = "test-issuer",
        jwtAudience = "test-audience",
        databaseUrl = "jdbc:h2:mem:unused",
        databaseUser = "sa",
        databasePassword = "",
        databaseDriver = "org.h2.Driver",
        corsAllowedOrigins = null,
        ollamaBaseUrl = baseUrl,
        ollamaModel = "gemma4:26b",
        unsplashAccessKey = null,
    )

    @Test
    fun enrichParsesWellFormedJsonResponse() = runBlocking {
        val llmResponse = """
            {"summary":"A tasty dish","tags":["quick","vegetarian"],
             "links":[{"site":"AllRecipes","searchQuery":"pasta primavera","description":"desc"}],
             "imageSearchQuery":"pasta"}
        """.trimIndent()
        FakeLlmServer.respondingWith(llmResponse).use { fake ->
            val service = EnrichmentService(configFor(fake.baseUrl))
            val result = service.enrich("Pasta", "Boil pasta, add sauce")
            assertEquals("A tasty dish", result.summary)
            assertEquals(listOf("quick", "vegetarian"), result.tags)
            assertEquals(1, result.links.size)
            assertEquals("AllRecipes", result.links.first().title)
            assertTrue(result.links.first().url.startsWith("https://www.allrecipes.com/search?q="))
            // No Unsplash key configured, so no image lookup is attempted.
            assertEquals(null, result.imageUrl)
        }
    }

    @Test
    fun enrichFallsBackToRawTextWhenResponseIsNotJson() = runBlocking {
        FakeLlmServer.respondingWith("not valid json at all").use { fake ->
            val service = EnrichmentService(configFor(fake.baseUrl))
            val result = service.enrich("Pasta", "Boil pasta")
            assertEquals("not valid json at all", result.summary)
            assertTrue(result.tags.isEmpty())
        }
    }

    @Test
    fun enrichStripsMarkdownCodeFences() = runBlocking {
        val fenced = "```json\n{\"summary\":\"Fenced\",\"tags\":[],\"links\":[],\"imageSearchQuery\":\"\"}\n```"
        FakeLlmServer.respondingWith(fenced).use { fake ->
            val service = EnrichmentService(configFor(fake.baseUrl))
            val result = service.enrich("Pasta", "Boil pasta")
            assertEquals("Fenced", result.summary)
        }
    }

    @Test
    fun enrichThrowsUnavailableWhenServerReturnsErrorStatus() {
        FakeLlmServer.respondingWithStatus(500).use { fake ->
            val service = EnrichmentService(configFor(fake.baseUrl))
            assertFailsWith<EnrichmentService.EnrichmentUnavailableException> {
                runBlocking { service.enrich("Pasta", "Boil pasta") }
            }
        }
    }

    @Test
    fun enrichThrowsUnavailableWhenServerIsUnreachable() {
        val service = EnrichmentService(configFor(FakeLlmServer.unreachableUrl()))
        assertFailsWith<EnrichmentService.EnrichmentUnavailableException> {
            runBlocking { service.enrich("Pasta", "Boil pasta") }
        }
    }
}
