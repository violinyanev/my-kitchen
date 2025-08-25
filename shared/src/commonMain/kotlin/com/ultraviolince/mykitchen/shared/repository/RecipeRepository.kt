package com.ultraviolince.mykitchen.shared.repository

import com.ultraviolince.mykitchen.shared.model.Recipe
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    fun observeRecipes(): Flow<List<Recipe>>
    suspend fun get(id: Long): Recipe?
    suspend fun upsert(recipe: Recipe): Long
    suspend fun delete(id: Long)
}

