package com.ultraviolince.mykitchen.recipes.data.repository

import com.ultraviolince.mykitchen.recipes.data.datasource.backend.JsRecipeServiceWrapper
import com.ultraviolince.mykitchen.recipes.data.datasource.localstorage.LocalStorageRecipeDao
import com.ultraviolince.mykitchen.recipes.data.datasource.localstorage.entity.Recipe as LocalRecipe
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
import com.ultraviolince.mykitchen.recipes.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class JsRecipeRepositoryImpl(
    private val dao: LocalStorageRecipeDao,
    private val recipeService: JsRecipeServiceWrapper,
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
        return dao.getRecipeById(id)?.toSharedRecipe()
    }

    override suspend fun insertRecipe(recipe: Recipe): Long {
        val localRecipe = LocalRecipe.fromSharedRecipe(recipe)
        val recipeId = dao.insertRecipe(localRecipe)
        recipeService.insertRecipe(recipeId, recipe)
        return recipeId
    }

    override suspend fun deleteRecipe(recipe: Recipe) {
        recipe.id?.let { id ->
            val localRecipe = LocalRecipe.fromSharedRecipe(recipe)
            recipeService.deleteRecipe(id)
            dao.deleteRecipe(localRecipe)
        }
    }
}