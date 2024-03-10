package com.ultraviolince.mykitchen.recipes.data.repository

import com.ultraviolince.mykitchen.recipes.data.datasource.backend.RecipeServiceWrapper
import com.ultraviolince.mykitchen.recipes.data.datasource.localdb.RecipeDao
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.repository.CloudSyncState
import com.ultraviolince.mykitchen.recipes.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class RecipeRepositoryImpl(
    private val dao: RecipeDao,
    private val recipeService: RecipeServiceWrapper
) : RecipeRepository {

    private val cloudSyncState = MutableStateFlow<CloudSyncState>(CloudSyncState.NotLoggedIn)

    override suspend fun login(server: String, email: String, password: String) {
        cloudSyncState.emit(CloudSyncState.LoginPending)
        val loginResult = recipeService.login(server = server, email = email, password = password)
        cloudSyncState.emit(loginResult)

        if (loginResult == CloudSyncState.SyncInProgress) {
            recipeService.sync(dao)
        }
    }

    override fun getLoginState(): Flow<CloudSyncState> {
        return cloudSyncState
    }

    override fun getRecipes(): Flow<List<Recipe>> {
        return dao.getRecipes()
    }

    override suspend fun getRecipeById(id: Long): Recipe? {
        return dao.getRecipeById(id)
    }

    override suspend fun insertRecipe(recipe: Recipe): Long {
        val recipeId = dao.insertRecipe(recipe)
        recipeService.insertRecipe(recipeId, recipe)
        return recipeId
    }

    override suspend fun deleteRecipe(recipe: Recipe) {
        recipeService.deleteRecipe(recipe.id!!)
        return dao.deleteRecipe(recipe)
    }
}
