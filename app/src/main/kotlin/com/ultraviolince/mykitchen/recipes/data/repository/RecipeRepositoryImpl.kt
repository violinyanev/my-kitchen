package com.ultraviolince.mykitchen.recipes.data.repository

import com.ultraviolince.mykitchen.recipes.data.datasource.backend.RecipeServiceWrapper
import com.ultraviolince.mykitchen.recipes.data.datasource.localdb.RecipeDao
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
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
        recipeService.insertRecipe(recipeId, recipe)
        return recipeId
    }

    override suspend fun deleteRecipe(recipe: Recipe) {
        // Only delete if recipe has an ID (i.e., it was saved before)
        recipe.id?.let { id ->
            recipeService.deleteRecipe(id)
            dao.deleteRecipe(recipe)
        }
    }
}
