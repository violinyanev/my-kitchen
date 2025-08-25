package com.ultraviolince.mykitchen.shared

import com.ultraviolince.mykitchen.shared.model.Recipe
import com.ultraviolince.mykitchen.shared.network.Recipes
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.resources.get
import io.ktor.resources.post
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class RecipesApiClient(private val baseUrl: String, private val tokenProvider: () -> String?) {
    private val client = HttpClient {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        expectSuccess = false
    }

    suspend fun fetchRecipes(): List<Recipe> {
        val response = client.get("$baseUrl/recipes")
        if (response.status.value in 200..299) {
            return response.body()
        }
        return emptyList()
    }

    suspend fun create(recipe: Recipe): Boolean {
        val response = client.post("$baseUrl/recipes") {
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "id" to (recipe.id ?: -1L),
                    "title" to recipe.title,
                    "ingredients" to recipe.ingredients,
                    "content" to recipe.content,
                    "timestamp" to recipe.createdAtEpochMillis
                )
            )
        }
        return response.status.value in 200..299
    }

    suspend fun delete(id: Long): Boolean {
        val response = client.delete("$baseUrl/recipes/$id")
        return response.status.value in 200..299
    }
}

