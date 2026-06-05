package com.ultraviolince.mykitchen.data.local

import com.ultraviolince.mykitchen.domain.model.Recipe
import kotlinx.coroutines.flow.Flow

interface RecipeDao {
    fun getRecipesByTitle(): Flow<List<Recipe>>
    fun getRecipesByDate(): Flow<List<Recipe>>
    suspend fun getAllActive(): List<Recipe>
    suspend fun getUnsyncedDeletedIds(): List<String>
    suspend fun getById(id: String): Recipe?
    suspend fun insert(recipe: Recipe)
    suspend fun insertAll(recipes: List<Recipe>)
    suspend fun softDelete(id: String)
    suspend fun markSynced(id: String)
    suspend fun clearSyncedDeleted()
}
