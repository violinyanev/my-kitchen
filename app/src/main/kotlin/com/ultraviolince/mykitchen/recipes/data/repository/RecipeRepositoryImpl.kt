package com.ultraviolince.mykitchen.recipes.data.repository

import com.ultraviolince.mykitchen.recipes.data.analytics.AnalyticsManager
import com.ultraviolince.mykitchen.recipes.data.analytics.AnalyticsConfig
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.RecipeServiceWrapper
import com.ultraviolince.mykitchen.recipes.data.datasource.localdb.RecipeDao
import com.ultraviolince.mykitchen.recipes.data.datasource.localdb.entity.Recipe as LocalRecipe
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
import com.ultraviolince.mykitchen.recipes.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RecipeRepositoryImpl(
    private val dao: RecipeDao,
    private val recipeService: RecipeServiceWrapper,
    private val analyticsManager: AnalyticsManager,
) : RecipeRepository {

    override suspend fun login(server: String, email: String, password: String) {
        try {
            recipeService.login(server = server, email = email, password = password)
            analyticsManager.trackAuthEvent(AnalyticsConfig.AuthActions.LOGIN, success = true)
        } catch (e: Exception) {
            analyticsManager.trackAuthEvent(AnalyticsConfig.AuthActions.LOGIN, success = false)
            analyticsManager.recordException(e, "Login failed")
            throw e
        }
    }

    override suspend fun logout() {
        try {
            recipeService.logout()
            analyticsManager.trackAuthEvent(AnalyticsConfig.AuthActions.LOGOUT, success = true)
        } catch (e: Exception) {
            analyticsManager.trackAuthEvent(AnalyticsConfig.AuthActions.LOGOUT, success = false)
            analyticsManager.recordException(e, "Logout failed")
            throw e
        }
    }

    override fun getLoginState(): Flow<LoginState> {
        return recipeService.loginState
    }

    override fun getRecipes(): Flow<List<Recipe>> {
        analyticsManager.trackRecipeEvent(AnalyticsConfig.RecipeActions.LIST)
        return dao.getRecipes().map { localRecipes ->
            localRecipes.map { it.toSharedRecipe() }
        }
    }

    override suspend fun getRecipeById(id: Long): Recipe? {
        analyticsManager.trackRecipeEvent(AnalyticsConfig.RecipeActions.READ, id)
        return dao.getRecipeById(id)?.toSharedRecipe()
    }

    override suspend fun insertRecipe(recipe: Recipe): Long {
        try {
            val localRecipe = LocalRecipe.fromSharedRecipe(recipe)
            val recipeId = dao.insertRecipe(localRecipe)
            recipeService.insertRecipe(recipeId, recipe)
            analyticsManager.trackRecipeEvent(AnalyticsConfig.RecipeActions.CREATE, recipeId)
            return recipeId
        } catch (e: Exception) {
            analyticsManager.recordException(e, "Recipe creation failed")
            throw e
        }
    }

    override suspend fun deleteRecipe(recipe: Recipe) {
        try {
            recipe.id?.let { id ->
                val localRecipe = LocalRecipe.fromSharedRecipe(recipe)
                recipeService.deleteRecipe(id)
                dao.deleteRecipe(localRecipe)
                analyticsManager.trackRecipeEvent(AnalyticsConfig.RecipeActions.DELETE, id)
            }
        } catch (e: Exception) {
            analyticsManager.recordException(e, "Recipe deletion failed")
            throw e
        }
    }
}
