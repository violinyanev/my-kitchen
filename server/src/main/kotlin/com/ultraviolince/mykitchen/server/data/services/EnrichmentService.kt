package com.ultraviolince.mykitchen.server.data.services

import com.ultraviolince.mykitchen.server.config.AppConfig
import com.ultraviolince.mykitchen.server.data.dto.RecipeLinkDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration

/**
 * Recipe enrichment powered by a locally-hosted llama.cpp model (no paid API).
 * The server POSTs to llama.cpp's OpenAI-compatible /v1/chat/completions endpoint
 * with the conversation so far and asks for a JSON object matching
 * [EnrichmentJsonResponse].
 */
class EnrichmentService(private val config: AppConfig) {

    private val logger = LoggerFactory.getLogger(EnrichmentService::class.java)

    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    data class EnrichmentResult(
        val summary: String,
        val tags: List<String>,
        val links: List<RecipeLinkDto>,
        val imageUrl: String?,
        val imageCredit: String?,
        val conversationHistory: String,
    )

    /** Thrown when the enrichment backend (llama.cpp) is unreachable or returns an error. */
    class EnrichmentUnavailableException(message: String, cause: Throwable? = null) :
        Exception(message, cause)

    @Serializable
    internal data class ConversationMessage(val role: String, val content: String)

    @Serializable
    private data class ResponseFormat(val type: String = "json_object")

    @Serializable
    private data class ChatRequest(
        val model: String,
        val messages: List<ConversationMessage>,
        val stream: Boolean = false,
        @SerialName("response_format")
        val responseFormat: ResponseFormat = ResponseFormat(),
        // Gemma 4 spends ~800-1300 tokens on reasoning (reasoning_content)
        // before the JSON answer, so the cap must cover thinking + answer.
        // It exists to bound runaway generations, not to squeeze latency.
        @SerialName("max_tokens")
        val maxTokens: Int = 2048,
    )

    @Serializable
    private data class ChatChoice(val message: ConversationMessage? = null)

    // OpenAI-compatible chat completion response: { "choices": [ { "message": {...} } ] }
    @Serializable
    private data class ChatResponse(val choices: List<ChatChoice> = emptyList())

    @Serializable
    private data class EnrichmentJsonResponse(
        val summary: String = "",
        val tags: List<String> = emptyList(),
        val links: List<EnrichmentJsonLink> = emptyList(),
        val imageSearchQuery: String = "",
    )

    @Serializable
    private data class EnrichmentJsonLink(
        val site: String = "",
        val searchQuery: String = "",
        val description: String = "",
    )

    suspend fun enrich(recipeTitle: String, recipeContent: String): EnrichmentResult {
        val userMessage = "Recipe:\nTitle: $recipeTitle\n\n$recipeContent"
        val history = mutableListOf(ConversationMessage("user", userMessage))

        val responseText = callOllama(history)
        history.add(ConversationMessage("assistant", responseText))

        return buildResult(responseText, history)
    }

    private suspend fun callOllama(history: List<ConversationMessage>): String =
        withContext(Dispatchers.IO) {
            val messages = buildList {
                add(ConversationMessage("system", systemPrompt))
                addAll(history)
            }
            val requestBody = json.encodeToString(
                ChatRequest(model = config.ollamaModel, messages = messages),
            )
            val request = HttpRequest.newBuilder()
                .uri(URI.create("${config.ollamaBaseUrl.trimEnd('/')}/v1/chat/completions"))
                .header("Content-Type", "application/json")
                // Generations are serialized on the LLM server, so this must cover
                // queue wait + generation. Kept below the web client's 10 min so
                // the backend returns a clean 503 before the client aborts.
                .timeout(Duration.ofMinutes(9))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build()
            val response = try {
                httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            } catch (e: java.io.IOException) {
                throw EnrichmentUnavailableException("Could not reach the LLM server at ${config.ollamaBaseUrl}", e)
            }
            if (response.statusCode() != 200) {
                throw EnrichmentUnavailableException(
                    "LLM request failed (${response.statusCode()}): ${response.body()}",
                )
            }
            json.decodeFromString<ChatResponse>(response.body()).choices.firstOrNull()?.message?.content.orEmpty()
        }

    private suspend fun buildResult(
        responseText: String,
        history: List<ConversationMessage>,
    ): EnrichmentResult {
        val parsed = try {
            json.decodeFromString<EnrichmentJsonResponse>(extractJson(responseText))
        } catch (e: Exception) {
            logger.error(
                "Failed to parse LLM JSON response (parse error: ${e::class.simpleName}); " +
                    "falling back to raw text summary. Response was: ${responseText.take(200)}",
                e,
            )
            EnrichmentJsonResponse(summary = responseText.take(500))
        }

        val (imageUrl, imageCredit) =
            if (parsed.imageSearchQuery.isNotBlank() && config.unsplashAccessKey != null) {
                fetchUnsplashImage(parsed.imageSearchQuery)
            } else {
                null to null
            }

        return EnrichmentResult(
            summary = parsed.summary,
            tags = parsed.tags,
            links = parsed.links.mapNotNull { link ->
                val url = siteSearchUrl(link.site, link.searchQuery) ?: return@mapNotNull null
                RecipeLinkDto(link.site, url, link.description)
            },
            imageUrl = imageUrl,
            imageCredit = imageCredit,
            conversationHistory = Json.encodeToString(history),
        )
    }

    private suspend fun fetchUnsplashImage(query: String): Pair<String?, String?> =
        withContext(Dispatchers.IO) {
            try {
                val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8)
                val unsplashUrl = "https://api.unsplash.com/search/photos" +
                    "?query=$encodedQuery&per_page=1&orientation=landscape"
                val request = HttpRequest.newBuilder()
                    .uri(URI.create(unsplashUrl))
                    .header("Authorization", "Client-ID ${config.unsplashAccessKey}")
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build()
                val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
                if (response.statusCode() != 200) return@withContext null to null

                val body = Json.parseToJsonElement(response.body()).jsonObject
                val result = body["results"]?.jsonArray?.firstOrNull()?.jsonObject
                    ?: return@withContext null to null

                val imageUrl = result["urls"]?.jsonObject?.get("regular")?.jsonPrimitive?.content
                val credit = result["user"]?.jsonObject?.get("name")?.jsonPrimitive?.content

                imageUrl to credit
            } catch (e: java.io.IOException) {
                logger.error("Network error fetching Unsplash image for query \"$query\"", e)
                null to null
            } catch (e: Exception) {
                logger.error(
                    "Failed to fetch or parse Unsplash image for query \"$query\" " +
                        "(${e::class.simpleName})",
                    e,
                )
                null to null
            }
        }

    private fun extractJson(text: String): String {
        val stripped = text.trim()
        return when {
            stripped.startsWith("```json") ->
                stripped.removePrefix("```json").removeSuffix("```").trim()
            stripped.startsWith("```") ->
                stripped.removePrefix("```").removeSuffix("```").trim()
            else -> stripped
        }
    }

    companion object {
        private val siteSearchTemplates = mapOf(
            "AllRecipes" to { q: String -> "https://www.allrecipes.com/search?q=${URLEncoder.encode(q, StandardCharsets.UTF_8)}" },
            "BBC Good Food" to { q: String -> "https://www.bbcgoodfood.com/search?q=${URLEncoder.encode(q, StandardCharsets.UTF_8)}" },
            "Food Network" to { q: String -> "https://www.foodnetwork.com/search/${URLEncoder.encode(q, StandardCharsets.UTF_8)}-" },
            "Serious Eats" to { q: String -> "https://www.seriouseats.com/search?q=${URLEncoder.encode(q, StandardCharsets.UTF_8)}" },
            "NYT Cooking" to { q: String -> "https://cooking.nytimes.com/search?q=${URLEncoder.encode(q, StandardCharsets.UTF_8)}" },
        )

        fun siteSearchUrl(site: String, searchQuery: String): String? =
            siteSearchTemplates[site]?.invoke(searchQuery.trim())?.takeIf { searchQuery.isNotBlank() }

        private val systemPrompt = """
            You are a culinary assistant helping make recipe cards more beautiful and useful.

            When given a recipe, respond with a JSON object:
            {
              "summary": "A brief, appetizing description of the dish (2-3 sentences)",
              "tags": ["pick only from: healthy, quick, vegetarian, vegan, kids-friendly, gluten-free, dairy-free, budget-friendly, meal-prep"],
              "links": [
                {"site": "AllRecipes", "searchQuery": "keywords to search for on this site", "description": "..."}
              ],
              "imageSearchQuery": "specific search query for a food photo of this dish"
            }

            For links, suggest 3-5 entries using these exact site names: AllRecipes, BBC Good Food, Food Network, Serious Eats, NYT Cooking.
            Provide a relevant search query for each site — do NOT invent specific URLs.

            When the user sends follow-up feedback, update your response accordingly while keeping the same JSON structure.

            Respond with ONLY valid JSON, no markdown code blocks, no explanations.
        """.trimIndent()
    }
}
