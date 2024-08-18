package data.repository

import data.datasource.backend.RecipeServiceWrapper
import domain.model.Recipe
import domain.repository.LoginState
import domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

class RecipeRepositoryImpl(
    //private val dao: RecipeDao,
    private val recipeService: RecipeServiceWrapper
) : RecipeRepository {

    private val r1 =  Recipe(title = "test1", content = "content1", timestamp = 1L, id = 1)
    private val r2 =  Recipe(title = "test2", content = "content2", timestamp = 1L, id = 2)
    private val staticRecipes =
        listOf(
            r1, r2
        )

    private val loginState = MutableStateFlow<LoginState>(LoginState.LoginEmpty)

    override suspend fun login(server: String, email: String, password: String) {
        loginState.emit(LoginState.LoginPending)
        val loginResult = recipeService.login(server = server, email = email, password = password)
        loginState.emit(loginResult)

        if (loginResult == LoginState.LoginSuccess) {
            // TODO kmp recipeService.sync(dao)
        }
    }

    override fun getLoginState(): Flow<LoginState> {
        return loginState
    }

    override fun getRecipes(): Flow<List<Recipe>> {
        return flowOf(
            listOf(
                Recipe(title = "test1", content = "content1", timestamp = 1L, id = 1),
                Recipe(title = "test2", content = "content2", timestamp = 1L, id = 2),
            )
        )//dao.getRecipes()
    }

    override suspend fun getRecipeById(id: Long): Recipe? {
        return null //dao.getRecipeById(id)
    }

    override suspend fun insertRecipe(recipe: Recipe): Long {
        val recipeId = 0L //dao.insertRecipe(recipe)
        recipeService.insertRecipe(recipeId, recipe)
        return recipeId
    }

    override suspend fun deleteRecipe(recipe: Recipe) {
        recipeService.deleteRecipe(recipe.id!!)
        //dao.deleteRecipe(recipe)
    }
}
