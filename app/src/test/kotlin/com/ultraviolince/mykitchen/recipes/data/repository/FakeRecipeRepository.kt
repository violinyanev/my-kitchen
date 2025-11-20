package com.ultraviolince.mykitchen.recipes.data.repository

import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.model.SyncStatus
import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
import com.ultraviolince.mykitchen.recipes.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking

data class RecipeWithSyncInfo(
    val recipe: Recipe,
    val syncStatus: SyncStatus = SyncStatus.NOT_SYNCED,
    val lastSyncTimestamp: Long? = null,
    val syncErrorMessage: String? = null
)

class FakeRecipeRepository : RecipeRepository {

    private val recipesWithSync = mutableListOf<RecipeWithSyncInfo>()

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
        return flow { emit(recipesWithSync.map { it.recipe }) }
    }

    fun getTestRecipes(): List<Recipe> {
        return recipesWithSync.map { it.recipe }
    }

    override suspend fun getRecipeById(id: Long): Recipe? {
        return recipesWithSync.find { it.recipe.id == id }?.recipe
    }

    override suspend fun insertRecipe(recipe: Recipe): Long {
        recipesWithSync.add(RecipeWithSyncInfo(recipe))
        return recipesWithSync.size.toLong()
    }

    override suspend fun deleteRecipe(recipe: Recipe) {
        recipesWithSync.removeIf { it.recipe.id == recipe.id }
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
        return recipesWithSync.filter { it.syncStatus == syncStatus }.map { it.recipe }
    }

    override suspend fun updateRecipeSyncStatus(
        recipeId: Long,
        syncStatus: SyncStatus,
        lastSyncTimestamp: Long?,
        syncErrorMessage: String?
    ) {
        val recipeIndex = recipesWithSync.indexOfFirst { it.recipe.id == recipeId }
        if (recipeIndex != -1) {
            val recipeWithSync = recipesWithSync[recipeIndex]
            recipesWithSync[recipeIndex] = recipeWithSync.copy(
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
