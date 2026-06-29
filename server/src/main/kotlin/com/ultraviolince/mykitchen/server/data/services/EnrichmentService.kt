package com.ultraviolince.mykitchen.server.data.services

import com.anthropic.client.AnthropicClient
import com.anthropic.client.okhttp.AnthropicOkHttpClient
import com.anthropic.models.messages.MessageCreateParams
import com.anthropic.models.messages.MessageParam
import com.ultraviolince.mykitchen.server.config.AppConfig
import com.ultraviolince.mykitchen.server.data.dto.RecipeLinkDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

class EnrichmentService(private val config: AppConfig) {

    private val anthropicClient: AnthropicClient = AnthropicOkHttpClient.builder()
        .apiKey(config.anthropicApiKey)
        .build()

    private val httpClient: HttpClient = HttpClient.newHttpClient()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    data class EnrichmentResult(
        val summary: String,
        val tags: List<String>,
        val links: List<RecipeLinkDto>,
        val imageUrl: String?,
        val imageCredit: String?,
        val conversationHistory: String,
    )

    @Serializable
    internal data class ConversationMessage(val role: String, val content: String)

    @Serializable
    private data class ClaudeRecipeResponse(
        val summary: String = "",
        val tags: List<String> = emptyList(),
        val links: List<ClaudeLink> = emptyList(),
        val imageSearchQuery: String = "",
    )

    @Serializable
    private data class ClaudeLink(
        val title: String = "",
        val url: String = "",
        val description: String = "",
    )

    suspend fun enrich(recipeTitle: String, recipeContent: String): EnrichmentResult {
        val userMessage = "Recipe:\nTitle: $recipeTitle\n\n$recipeContent"
        val history = mutableListOf(ConversationMessage("user", userMessage))

        val params = MessageCreateParams.builder()
            .model("claude-opus-4-8")
            .maxTokens(2000L)
            .system(systemPrompt)
            .addUserMessage(userMessage)
            .build()

        val responseText = callClaude(params)
        history.add(ConversationMessage("assistant", responseText))

        return buildResult(responseText, history)
    }

    suspend fun refine(feedback: String, storedHistory: String): EnrichmentResult {
        val history = json.decodeFromString<List<ConversationMessage>>(storedHistory).toMutableList()
        history.add(ConversationMessage("user", feedback))

        val messages = history.map { msg ->
            MessageParam.builder()
                .role(if (msg.role == "user") MessageParam.Role.USER else MessageParam.Role.ASSISTANT)
                .content(MessageParam.Content.ofString(msg.content))
                .build()
        }

        val params = MessageCreateParams.builder()
            .model("claude-opus-4-8")
            .maxTokens(2000L)
            .system(systemPrompt)
            .messages(messages)
            .build()

        val responseText = callClaude(params)
        history.add(ConversationMessage("assistant", responseText))

        return buildResult(responseText, history)
    }

    private suspend fun callClaude(params: MessageCreateParams): String =
        withContext(Dispatchers.IO) {
            anthropicClient.messages().create(params)
                .content()
                .filter { it.isText() }
                .joinToString("") { it.asText().text() }
        }

    private suspend fun buildResult(
        responseText: String,
        history: List<ConversationMessage>,
    ): EnrichmentResult {
        val parsed = try {
            json.decodeFromString<ClaudeRecipeResponse>(extractJson(responseText))
        } catch (_: Exception) {
            ClaudeRecipeResponse(summary = responseText.take(500))
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
            links = parsed.links.map { RecipeLinkDto(it.title, it.url, it.description) },
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
            } catch (_: Exception) {
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
        private val systemPrompt = """
            You are a culinary assistant helping make recipe cards more beautiful and useful.

            When given a recipe, respond with a JSON object:
            {
              "summary": "A brief, appetizing description of the dish (2-3 sentences)",
              "tags": ["pick only from: healthy, quick, vegetarian, vegan, kids-friendly, gluten-free, dairy-free, budget-friendly, meal-prep"],
              "links": [
                {"title": "...", "url": "https://...", "description": "..."}
              ],
              "imageSearchQuery": "specific search query for a food photo of this dish"
            }

            For links, provide 3-5 URLs from reputable cooking sites (AllRecipes, BBC Good Food, Food Network, Serious Eats, or NYT Cooking) relevant to this type of recipe.

            When the user sends follow-up feedback, update your response accordingly while keeping the same JSON structure.

            Respond with ONLY valid JSON, no markdown code blocks, no explanations.
        """.trimIndent()
    }
}
