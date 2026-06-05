package com.ultraviolince.mykitchen.data.fake

import com.ultraviolince.mykitchen.data.local.RecipeDao
import com.ultraviolince.mykitchen.domain.model.Recipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeRecipeDao : RecipeDao {
    private val recipes = MutableStateFlow<List<Recipe>>(emptyList())

    override fun getRecipesByTitle(): Flow<List<Recipe>> =
        recipes.map { it.filter { r -> !r.deleted }.sortedBy { r -> r.title } }

    override fun getRecipesByDate(): Flow<List<Recipe>> =
        recipes.map { it.filter { r -> !r.deleted }.sortedByDescending { r -> r.timestamp } }

    override suspend fun getAllActive(): List<Recipe> =
        recipes.value.filter { !it.deleted }

    override suspend fun getUnsyncedDeletedIds(): List<String> =
        recipes.value.filter { it.deleted && !it.synced }.map { it.id }

    override suspend fun getById(id: String): Recipe? =
        recipes.value.find { it.id == id }

    override suspend fun insert(recipe: Recipe) {
        val current = recipes.value.filter { it.id != recipe.id }.toMutableList()
        current.add(recipe)
        recipes.value = current
    }

    override suspend fun insertAll(recipes: List<Recipe>) {
        recipes.forEach { insert(it) }
    }

    override suspend fun softDelete(id: String) {
        this.recipes.value = this.recipes.value.map {
            if (it.id == id) it.copy(deleted = true, synced = false) else it
        }
    }

    override suspend fun markSynced(id: String) {
        recipes.value = recipes.value.map {
            if (it.id == id) it.copy(synced = true) else it
        }
    }

    override suspend fun clearSyncedDeleted() {
        recipes.value = recipes.value.filter { !(it.deleted && it.synced) }
    }
}

