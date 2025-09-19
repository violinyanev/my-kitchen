package com.ultraviolince.mykitchen.recipes.data.repository

import com.ultraviolince.mykitchen.firebase.FirebaseManager
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
    private val firebaseManager: FirebaseManager,
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
        return dao.getRecipes().map { localRecipes ->
            localRecipes.map { it.toSharedRecipe() }
        }
    }

    override suspend fun getRecipeById(id: Long): Recipe? {
        val recipe = dao.getRecipeById(id)?.toSharedRecipe()
        recipe?.let {
            firebaseManager.getAnalytics().logRecipeViewed(it.title)
        }
        return recipe
    }

    override suspend fun insertRecipe(recipe: Recipe): Long {
        try {
            val localRecipe = LocalRecipe.fromSharedRecipe(recipe)
            val recipeId = dao.insertRecipe(localRecipe)
            recipeService.insertRecipe(recipeId, recipe)
            firebaseManager.getAnalytics().logRecipeCreated(recipe.title)
            return recipeId
        } catch (e: Exception) {
            firebaseManager.logError(e, "insertRecipe")
            throw e
        }
    }

    override suspend fun deleteRecipe(recipe: Recipe) {
        recipe.id?.let { id ->
            try {
                val localRecipe = LocalRecipe.fromSharedRecipe(recipe)
                recipeService.deleteRecipe(id)
                dao.deleteRecipe(localRecipe)
                firebaseManager.getAnalytics().logEvent("recipe_deleted", mapOf("recipe_name" to recipe.title))
            } catch (e: Exception) {
                firebaseManager.logError(e, "deleteRecipe")
                throw e
            }
        }
    }
}
