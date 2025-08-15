package com.ultraviolince.mykitchen.recipes.domain.repository

import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    fun getRecipes(): Flow<List<Recipe>>

    suspend fun getRecipeById(id: Long): Recipe?

    suspend fun insertRecipe(recipe: Recipe): Long

    suspend fun deleteRecipe(recipe: Recipe)

    suspend fun login(server: String, email: String, password: String)

    suspend fun logout()

    fun getLoginState(): Flow<LoginState>
    
    // Sync-related methods
    suspend fun getRecipesBySyncStatus(syncStatus: SyncStatus): List<Recipe>
    
    suspend fun updateRecipeSyncStatus(
        recipeId: Long,
        syncStatus: SyncStatus,
        lastSyncTimestamp: Long? = null,
        syncErrorMessage: String? = null
    )
    
    suspend fun syncAllRecipes()
    
    suspend fun syncRecipe(recipeId: Long): Boolean
}
