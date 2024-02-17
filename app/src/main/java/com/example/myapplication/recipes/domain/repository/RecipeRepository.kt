package com.example.myapplication.recipes.domain.repository

import com.example.myapplication.recipes.domain.model.Recipe
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    fun getRecipes(): Flow<List<Recipe>>

    suspend fun getRecipeById(id: Long): Recipe?

    suspend fun insertRecipe(recipe: Recipe): Long

    suspend fun deleteRecipe(recipe: Recipe)

    suspend fun login(server: String, username: String, password: String)

    fun getLoginState(): Flow<LoginState>
}
