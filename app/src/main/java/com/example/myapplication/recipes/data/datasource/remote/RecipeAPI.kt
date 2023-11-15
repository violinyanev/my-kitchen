package com.example.myapplication.recipes.data.datasource.remote

import retrofit2.http.GET

interface RecipeAPI {
    @GET("/recipes")
    suspend fun getResipes(): ApiResponse
}
