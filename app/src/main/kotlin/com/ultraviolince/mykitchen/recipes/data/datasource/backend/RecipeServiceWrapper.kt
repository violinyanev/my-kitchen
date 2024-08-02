package com.ultraviolince.mykitchen.recipes.data.datasource.backend

import android.util.Log
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.data.BackendRecipe
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.data.LoginRequest
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.util.NetworkError
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.util.Result
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.util.map
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.util.onError
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.util.onSuccess
import com.ultraviolince.mykitchen.recipes.data.datasource.localdb.RecipeDao
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
import io.ktor.client.engine.cio.CIO

class RecipeServiceWrapper {

    private var recipeService: RecipeService? = null

    suspend fun login(server: String, email: String, password: String): LoginState {
        val tmpService = RecipeService(createHttpClient(CIO.create(), server, null))

        // TODO Store the token, don't force authentication all the time
        val result = tmpService.login(LoginRequest(email, password))

        Log.i("#network", "Login result: $result")
        return when (result) {
            is Result.Error -> LoginState.LoginFailure(error = result.error)
            is Result.Success -> LoginState.LoginSuccess
        }
//        result.onSuccess { data ->
//            Log.i("#network", "Successfully logged in: ${data.data.token}")
//            recipeService = RecipeService(createHttpClient(CIO.create(), server, data.data.token))
//            return LoginState.LoginSuccess
//        }
//        // TODO fix error handling
//        result.onError { error ->
//            Log.i("#network", "Failed to log in: ${error.name}")
//            return LoginState.LoginFailure(error = error)
//        }
//
//        return LoginState.LoginFailure(error = NetworkError.UNKNOWN)
    }

    suspend fun insertRecipe(recipeId: Long, recipe: Recipe): Boolean {
        Log.i("Recipes", "Syncing recipe to backend: $recipe")

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

            Log.i("#network", "Create recipe result: $result")
            return when (result) {
                is Result.Error -> false
                is Result.Success -> {
                    true
                }
            }
            result.onSuccess {
                Log.i("Recipes", "Created recipe at backend: $recipe")
                return true
            }
            result.onError {
                Log.i("Recipes", "Failed to create recipe on backend: $recipe")
                return false
            }
        }

        return false
    }

    suspend fun deleteRecipe(recipeId: Long): Boolean {
        Log.i("Recipes", "Deleting recipe from backend: $recipeId")
        // TODO make this safe by design

        return recipeId == 1L

//        recipeService?.apply {
//            val result = deleteRecipe(
//                recipeId = recipeId
//            )
//
//            result.onSuccess {
//                Log.i("Recipes", "Deleted recipe from backend: $recipeId")
//                return true
//            }
//            result.onFailure {
//                Log.i("Recipes", "Failed to delete recipe from backend: $recipeId")
//                return false
//            }
//        }
//
//        return false
    }

    suspend fun sync(dao: RecipeDao) {
        recipeService?.apply {
            val existingRecipes = mutableSetOf<Long>()

            val maybeRecipes = getRecipes()

            maybeRecipes.onSuccess { recipes ->
                for (r in recipes.result) {
                    dao.insertRecipe(
                        Recipe(
                            id = r.id,
                            title = r.title,
                            content = r.body,
                            timestamp = r.timestamp
                        )
                    )
                    existingRecipes.add(r.id)
                }
            }
                /*val dbRecipes = dao.getRecipes()

                Log.e("RECIPES", "Before1")
                val currentDbRecipes = dbRecipes.last()

                Log.e("RECIPES", "Before2")

                for (r in currentDbRecipes) {
                    r.id?.let {
                        if(!existingRecipes.contains(it)){
                            insertRecipe(it, r)
                        }
                    }
                }
                Log.e("RECIPES", "After")*/
        }
    }
}
