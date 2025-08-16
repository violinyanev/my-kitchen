package com.ultraviolince.mykitchen.recipes.data.repository

import com.ultraviolince.mykitchen.recipes.data.datasource.backend.RecipeServiceWrapper
import com.ultraviolince.mykitchen.recipes.data.datasource.localdb.RecipeDao
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.model.SyncStatus
import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
import com.ultraviolince.mykitchen.recipes.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow

class RecipeRepositoryImpl(
    private val dao: RecipeDao,
    private val recipeService: RecipeServiceWrapper,
) : RecipeRepository {

    override suspend fun login(server: String, email: String, password: String) {
        recipeService.login(server = server, email = email, password = password)
    }

    override suspend fun logout() {
        recipeService.logout()
    }

    override fun getLoginState(): Flow<LoginState> {
        return recipeService.loginState
    }

    override fun getRecipes(): Flow<List<Recipe>> {
        return dao.getRecipes()
    }

    override suspend fun getRecipeById(id: Long): Recipe? {
        return dao.getRecipeById(id)
    }

    override suspend fun insertRecipe(recipe: Recipe): Long {
        val recipeId = dao.insertRecipe(recipe)
        // Mark as syncing and attempt to sync to backend
        dao.updateRecipeSyncStatus(recipeId, SyncStatus.SYNCING, System.currentTimeMillis())
        val syncSuccess = recipeService.insertRecipe(recipeId, recipe)

        // Update sync status based on result
        if (syncSuccess) {
            dao.updateRecipeSyncStatus(recipeId, SyncStatus.SYNCED, System.currentTimeMillis())
        } else {
            dao.updateRecipeSyncStatus(recipeId, SyncStatus.SYNC_ERROR, syncErrorMessage = "Failed to sync to server")
        }

        return recipeId
    }

    override suspend fun deleteRecipe(recipe: Recipe) {
        recipeService.deleteRecipe(recipe.id!!)
        return dao.deleteRecipe(recipe)
    }

    override suspend fun getRecipesBySyncStatus(syncStatus: SyncStatus): List<Recipe> {
        return dao.getRecipesBySyncStatus(syncStatus)
    }

    override suspend fun updateRecipeSyncStatus(
        recipeId: Long,
        syncStatus: SyncStatus,
        lastSyncTimestamp: Long?,
        syncErrorMessage: String?
    ) {
        dao.updateRecipeSyncStatus(recipeId, syncStatus, lastSyncTimestamp, syncErrorMessage)
    }

    override suspend fun syncAllRecipes() {
        recipeService.syncAllRecipes()
    }

    override suspend fun syncRecipe(recipeId: Long): Boolean {
        val recipe = dao.getRecipeById(recipeId) ?: return false
        dao.updateRecipeSyncStatus(recipeId, SyncStatus.SYNCING, System.currentTimeMillis())

        val syncSuccess = recipeService.insertRecipe(recipeId, recipe)

        if (syncSuccess) {
            dao.updateRecipeSyncStatus(recipeId, SyncStatus.SYNCED, System.currentTimeMillis())
        } else {
            dao.updateRecipeSyncStatus(recipeId, SyncStatus.SYNC_ERROR, syncErrorMessage = "Failed to sync to server")
        }

        return syncSuccess
    }
}
