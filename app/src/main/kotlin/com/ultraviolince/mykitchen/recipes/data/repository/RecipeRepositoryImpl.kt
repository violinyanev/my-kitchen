package com.ultraviolince.mykitchen.recipes.data.repository

import com.ultraviolince.mykitchen.recipes.data.datasource.backend.RecipeServiceWrapper
import com.ultraviolince.mykitchen.recipes.data.datasource.localdb.RecipeDao
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.model.User
import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
import com.ultraviolince.mykitchen.recipes.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class RecipeRepositoryImpl(
    private val dao: RecipeDao,
    private val recipeService: RecipeServiceWrapper
) : RecipeRepository {

    private val loginState = MutableStateFlow<LoginState>(LoginState.LoginEmpty)

    override suspend fun insertUser(user: User): Long {
        return dao.insertUser(user)
    }

    override suspend fun deleteUser(user: User) {
        return dao.deleteUser(user)
    }

    override suspend fun login(user: User, password: String?) {
        loginState.emit(LoginState.LoginPending)
        val loginResult = recipeService.login(user = user, password = password)
        loginState.emit(loginResult)

        if (loginResult == LoginState.LoginSuccess) {
            recipeService.sync(dao)
        }
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
        recipeService.insertRecipe(recipeId, recipe)
        return recipeId
    }

    override suspend fun deleteRecipe(recipe: Recipe) {
        recipeService.deleteRecipe(recipe.id!!)
        return dao.deleteRecipe(recipe)
    }

    override fun getUsers(): Flow<List<User>> {
        return dao.getUsers()
    }

    override suspend fun getUserById(id: Long): User? {
        return dao.getUserById(id)
    }

}
