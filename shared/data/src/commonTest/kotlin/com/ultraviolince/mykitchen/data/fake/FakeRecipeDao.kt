package com.ultraviolince.mykitchen.data.fake

import com.ultraviolince.mykitchen.data.local.RecipeDao
import com.ultraviolince.mykitchen.data.local.RecipeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeRecipeDao : RecipeDao {
    private val recipes = MutableStateFlow<List<RecipeEntity>>(emptyList())

    override fun getRecipesByTitle(): Flow<List<RecipeEntity>> =
        recipes.map { it.filter { e -> !e.deleted }.sortedBy { e -> e.title } }

    override fun getRecipesByDate(): Flow<List<RecipeEntity>> =
        recipes.map { it.filter { e -> !e.deleted }.sortedByDescending { e -> e.timestamp } }

    override suspend fun getAllActive(): List<RecipeEntity> =
        recipes.value.filter { !it.deleted }

    override suspend fun getUnsynced(): List<RecipeEntity> =
        recipes.value.filter { it.deleted && !it.synced }

    override suspend fun getById(id: String): RecipeEntity? =
        recipes.value.find { it.id == id }

    override suspend fun insert(recipe: RecipeEntity) {
        val current = recipes.value.toMutableList()
        current.removeIf { it.id == recipe.id }
        current.add(recipe)
        recipes.value = current
    }

    override suspend fun insertAll(recipes: List<RecipeEntity>) {
        recipes.forEach { insert(it) }
    }

    override suspend fun softDelete(id: String) {
        recipes.value = recipes.value.map {
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
