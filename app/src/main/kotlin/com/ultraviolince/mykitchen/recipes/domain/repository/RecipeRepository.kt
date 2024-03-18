package com.ultraviolince.mykitchen.recipes.domain.repository

import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    fun getRecipes(): Flow<List<Recipe>>

    suspend fun getRecipeById(id: Long): Recipe?

    suspend fun insertRecipe(recipe: Recipe): Long

    suspend fun deleteRecipe(recipe: Recipe)

    suspend fun login(server: String, email: String, password: String)

    fun getLoginState(): Flow<CloudSyncState>
}
