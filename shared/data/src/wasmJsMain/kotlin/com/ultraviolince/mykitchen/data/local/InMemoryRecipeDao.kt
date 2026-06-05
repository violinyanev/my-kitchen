package com.ultraviolince.mykitchen.data.local

import com.ultraviolince.mykitchen.domain.model.Recipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * In-memory [RecipeDao] implementation for the WasmJs/web platform where
 * Room is unavailable. Data is stored in memory and not persisted.
 */
class InMemoryRecipeDao : RecipeDao {
    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())

    override fun getRecipesByTitle(): Flow<List<Recipe>> =
        _recipes.map { list -> list.filter { !it.deleted }.sortedBy { it.title } }

    override fun getRecipesByDate(): Flow<List<Recipe>> =
        _recipes.map { list -> list.filter { !it.deleted }.sortedByDescending { it.timestamp } }

    override suspend fun getAllActive(): List<Recipe> =
        _recipes.value.filter { !it.deleted }

    override suspend fun getUnsyncedDeletedIds(): List<String> =
        _recipes.value.filter { it.deleted && !it.synced }.map { it.id }

    override suspend fun getById(id: String): Recipe? =
        _recipes.value.find { it.id == id }

    override suspend fun insert(recipe: Recipe) {
        _recipes.update { list ->
            val idx = list.indexOfFirst { it.id == recipe.id }
            if (idx >= 0) list.toMutableList().apply { set(idx, recipe) } else list + recipe
        }
    }

    override suspend fun insertAll(recipes: List<Recipe>) {
        recipes.forEach { insert(it) }
    }

    override suspend fun softDelete(id: String) {
        _recipes.update { list ->
            list.map { if (it.id == id) it.copy(deleted = true, synced = false) else it }
        }
    }

    override suspend fun markSynced(id: String) {
        _recipes.update { list ->
            list.map { if (it.id == id) it.copy(synced = true) else it }
        }
    }

    override suspend fun clearSyncedDeleted() {
        _recipes.update { list -> list.filter { !(it.deleted && it.synced) } }
    }
}

