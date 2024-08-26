package data.repository

import data.datasource.backend.RecipeServiceWrapper
import data.datasource.localdb.RecipeDao
import domain.model.Recipe
import domain.repository.LoginState
import domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class RecipeRepositoryImpl(
    private val dao: RecipeDao,
    private val recipeService: RecipeServiceWrapper,
    private val preferences: RecipePreferences
) : RecipeRepository {

    private val loginState = MutableStateFlow<LoginState>(LoginState.LoginEmpty)

    override suspend fun login(server: String, email: String, password: String) {
        loginState.emit(LoginState.LoginPending)
        val loginResult = recipeService.login(server = server, email = email, password = password, preferences = preferences)
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
        dao.deleteRecipe(recipe)
    }
}
