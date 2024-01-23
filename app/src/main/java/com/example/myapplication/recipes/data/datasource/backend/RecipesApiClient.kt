package com.example.myapplication.recipes.data.datasource.backend

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RecipesApiClient {

    // TODO Make configurable
    private const val BASE_URL = "https://ultraviolince.com:8019"

    val recipeService: RecipeService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RecipeService::class.java)
    }
}
