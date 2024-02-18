package com.example.myapplication.recipes.data.datasource.backend

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface RecipeService {

    @GET("/recipes")
    suspend fun getRecipes(): List<BackendRecipe>

    @POST("/recipes")
    suspend fun createRecipe(@Body recipeRequest: BackendRecipe): BackendRecipeResponse

    @DELETE("/recipes/{recipeId}")
    suspend fun deleteRecipe(@Path("recipeId") recipeId: Long): Response<Unit>

    @POST("/users/login")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResult
}
