package com.example.myapplication.recipes.data.repository

import com.example.myapplication.recipes.data.datasource.backend.RecipeServiceWrapper
import com.example.myapplication.recipes.data.datasource.localdb.RecipeDao
import com.example.myapplication.recipes.domain.model.Recipe
import com.example.myapplication.recipes.domain.repository.LoginState
import com.example.myapplication.recipes.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class RecipeRepositoryImpl(
    private val dao: RecipeDao,
    private val recipeService: RecipeServiceWrapper
) : RecipeRepository {

    private val loginState = MutableStateFlow<LoginState>(LoginState.LoginEmpty)

    override suspend fun login(server: String, email: String, password: String) {
        loginState.emit(LoginState.LoginPending)
        loginState.emit(recipeService.login(server = server, email = email, password = password))
    }

    override fun getLoginState(): Flow<LoginState> {
        return loginState
    }

    override fun getRecipes(): Flow<List<Recipe>> {
        return dao.getRecipes()
    }

    override suspend fun getRecipeById(id: Long): Recipe? {
        return dao.getRecipeById(id)
    }

    override suspend fun insertRecipe(recipe: Recipe): Long {
        val recipeId = dao.insertRecipe(recipe)
        recipeService?.insertRecipe(recipeId, recipe)
        return recipeId
    }

    override suspend fun deleteRecipe(recipe: Recipe) {
        return dao.deleteRecipe(recipe)
    }
}
