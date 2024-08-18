package data.datasource.backend

import data.datasource.backend.data.BackendRecipe
import data.datasource.backend.data.LoginRequest
import data.datasource.backend.util.NetworkResult
import domain.model.Recipe
import domain.repository.LoginState
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.logging.Logger

class RecipeServiceWrapper {

    private var recipeService: RecipeService? = null

    private val logger = object : Logger {
        override fun log(message: String) {
            Log.d(message)
        }
    }

    suspend fun login(server: String, email: String, password: String): LoginState {
        val tmpService = RecipeService(createHttpClient(CIO.create(), server, null, logger))

        // TODO Store the token, don't force authentication all the time
        val result = tmpService.login(LoginRequest(email, password))

        Log.i("Login result: $result")
        return when (result) {
            is NetworkResult.Error -> LoginState.LoginFailure(error = result.error)
            is NetworkResult.Success -> {
                recipeService = RecipeService(createHttpClient(CIO.create(), server, result.data.data.token, logger))
                LoginState.LoginSuccess
            }
        }
    }

    suspend fun insertRecipe(recipeId: Long, recipe: Recipe): Boolean {
        Log.i("Syncing recipe to backend: $recipe")

        // TODO make this safe by design
        recipeService?.apply {
            val result = createRecipe(
                BackendRecipe(
                    id = recipeId,
                    title = recipe.title,
                    body = recipe.content,
                    timestamp = recipe.timestamp
                )
            )

            Log.i("Create recipe result: $result")
            return when (result) {
                is NetworkResult.Error -> false
                is NetworkResult.Success -> {
                    true
                }
            }
        }

        return false
    }

    suspend fun deleteRecipe(recipeId: Long): Boolean {
        Log.i("Deleting recipe from backend: $recipeId")
        // TODO make this safe by design

        recipeService?.apply {
            val result = deleteRecipe(
                recipeId = recipeId
            )

            Log.i("Delete recipe result: $result")
            return when (result) {
                is NetworkResult.Error -> false
                is NetworkResult.Success -> true
            }
        }

        return false
    }

//    suspend fun sync(dao: RecipeDao) {
//        recipeService?.apply {
//            val existingRecipes = mutableSetOf<Long>()
//
//            val maybeRecipes = getRecipes()
//
//            maybeRecipes.onSuccess { recipes ->
//                for (r in recipes) {
//                    dao.insertRecipe(
//                        Recipe(
//                            id = r.id,
//                            title = r.title,
//                            content = r.body,
//                            timestamp = r.timestamp
//                        )
//                    )
//                    existingRecipes.add(r.id)
//                }
//            }
//                /*val dbRecipes = dao.getRecipes()
//
//                Log.e("Before1")
//                val currentDbRecipes = dbRecipes.last()
//
//                Log.e("Before2")
//
//                for (r in currentDbRecipes) {
//                    r.id?.let {
//                        if(!existingRecipes.contains(it)){
//                            insertRecipe(it, r)
//                        }
//                    }
//                }
//                Log.e("After")*/
//        }
//    }
}
