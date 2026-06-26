package com.ultraviolince.mykitchen.data.remote

import com.ultraviolince.mykitchen.data.remote.dto.LoginRequest
import com.ultraviolince.mykitchen.data.remote.dto.LoginResponse
import com.ultraviolince.mykitchen.data.remote.dto.RecipeDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class RecipeApiClient(private val httpClient: HttpClient) {

    suspend fun login(serverUrl: String, email: String, password: String): Result<LoginResponse> =
        runCatching {
            httpClient.post("$serverUrl/users/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email, password))
            }.body<LoginResponse>()
        }

    suspend fun getRecipes(serverUrl: String, token: String): Result<List<RecipeDto>> =
        runCatching {
            httpClient.get("$serverUrl/recipes") {
                bearerAuth(token)
            }.body<List<RecipeDto>>()
        }

    suspend fun createRecipe(serverUrl: String, token: String, recipe: RecipeDto): Result<RecipeDto> =
        runCatching {
            httpClient.post("$serverUrl/recipes") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(recipe)
            }.body<RecipeDto>()
        }

    suspend fun updateRecipe(serverUrl: String, token: String, recipe: RecipeDto): Result<RecipeDto> =
        runCatching {
            httpClient.put("$serverUrl/recipes/${recipe.id}") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(recipe)
            }.body<RecipeDto>()
        }

    suspend fun deleteRecipe(serverUrl: String, token: String, id: String): Result<Unit> =
        runCatching {
            httpClient.delete("$serverUrl/recipes/$id") {
                bearerAuth(token)
            }
            Unit
        }
}
