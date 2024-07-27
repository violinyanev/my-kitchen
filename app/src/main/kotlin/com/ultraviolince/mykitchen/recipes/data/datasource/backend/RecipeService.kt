package com.ultraviolince.mykitchen.recipes.data.datasource.backend

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class RecipeService(private val ktor: HttpClient) {

    suspend fun getRecipes(): Result<List<BackendRecipe>> = runCatching {
        ktor.get("/recipes").body()
    }

    suspend fun createRecipe(recipeRequest: BackendRecipe): Result<BackendRecipeResponse> = runCatching {
        ktor.post("/recipes") {
            contentType(ContentType.Application.Json)
            setBody(recipeRequest)
        }.body()
    }

    suspend fun deleteRecipe(recipeId: Long): Result<Unit> = runCatching {
        ktor.delete("/recipes/$recipeId").body()
    }

    suspend fun login(loginRequest: LoginRequest): Result<LoginResult> = runCatching {
        ktor.post("/users/login") {
            contentType(ContentType.Application.Json)
            setBody(loginRequest)
        }.body()
    }
}
