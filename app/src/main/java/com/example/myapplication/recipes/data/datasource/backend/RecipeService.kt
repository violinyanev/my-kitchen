package com.example.myapplication.recipes.data.datasource.backend

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface RecipeService {
    @GET("/recipes")
    fun getRecipes(): Call<List<BackendRecipe>>

    @POST("/recipes")
    fun createRecipe(@Body recipeRequest: BackendRecipe): Call<BackendRecipe>

    @DELETE("/recipes/{recipeId}")
    fun deleteRecipe(@Path("recipeId") recipeId: Int): Call<Void>
}
