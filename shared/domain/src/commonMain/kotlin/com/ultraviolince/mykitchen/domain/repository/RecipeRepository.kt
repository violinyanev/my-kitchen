package com.ultraviolince.mykitchen.domain.repository

import com.ultraviolince.mykitchen.domain.model.AuthState
import com.ultraviolince.mykitchen.domain.model.Recipe
import com.ultraviolince.mykitchen.domain.model.RecipeOrder
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    fun getRecipes(order: RecipeOrder): Flow<List<Recipe>>
    suspend fun getRecipeById(id: String): Recipe?
    suspend fun insertRecipe(recipe: Recipe)
    suspend fun deleteRecipe(id: String)
    suspend fun syncRecipes(): Result<Unit>
    suspend fun login(email: String, password: String, serverUrl: String): Result<Unit>
    suspend fun logout()
    fun getAuthState(): Flow<AuthState>
}
