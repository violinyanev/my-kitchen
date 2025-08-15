package com.ultraviolince.mykitchen.recipes.data.repository

import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.model.SyncStatus
import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
import com.ultraviolince.mykitchen.recipes.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking

class FakeRecipeRepository : RecipeRepository {

    private val recipes = mutableListOf<Recipe>()

    init {
        val recipesToInsert = mutableListOf<Recipe>()
        ('a'..'z').forEachIndexed { index, c ->
            recipesToInsert.add(
                Recipe(
                    title = c.toString(),
                    content = c.toString(),
                    timestamp = index.toLong(),
                    id = index.toLong()
                )
            )
        }
        recipesToInsert.shuffle()
        runBlocking {
            recipesToInsert.forEach { insertRecipe(it) }
        }
    }

    override fun getRecipes(): Flow<List<Recipe>> {
        return flow { emit(recipes) }
    }

    fun getTestRecipes(): List<Recipe> {
        return recipes
    }

    override suspend fun getRecipeById(id: Long): Recipe? {
        return recipes.find { it.id == id }
    }

    override suspend fun insertRecipe(recipe: Recipe): Long {
        recipes.add(recipe)
        return recipes.size.toLong()
    }

    override suspend fun deleteRecipe(recipe: Recipe) {
        recipes.remove(recipe)
    }

    override suspend fun login(server: String, email: String, password: String) {
    }

    override suspend fun logout() {
    }

    override fun getLoginState(): Flow<LoginState> {
        return flow {
            emit(LoginState.LoginEmpty)
        }
    }

    override suspend fun getRecipesBySyncStatus(syncStatus: SyncStatus): List<Recipe> {
        return recipes.filter { it.syncStatus == syncStatus }
    }

    override suspend fun updateRecipeSyncStatus(
        recipeId: Long,
        syncStatus: SyncStatus,
        lastSyncTimestamp: Long?,
        syncErrorMessage: String?
    ) {
        val recipeIndex = recipes.indexOfFirst { it.id == recipeId }
        if (recipeIndex != -1) {
            val recipe = recipes[recipeIndex]
            recipes[recipeIndex] = recipe.copy(
                syncStatus = syncStatus,
                lastSyncTimestamp = lastSyncTimestamp,
                syncErrorMessage = syncErrorMessage
            )
        }
    }

    override suspend fun syncAllRecipes() {
        // Mock implementation - do nothing for tests
    }

    override suspend fun syncRecipe(recipeId: Long): Boolean {
        // Mock implementation - return true for tests
        updateRecipeSyncStatus(recipeId, SyncStatus.SYNCED, System.currentTimeMillis())
        return true
    }
}
