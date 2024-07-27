package com.ultraviolince.mykitchen.recipes.data.datasource.backend

import android.util.Log
import com.ultraviolince.mykitchen.R
import com.ultraviolince.mykitchen.recipes.data.datasource.localdb.RecipeDao
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.resources.Resources
import io.ktor.serialization.gson.gson

fun createHttpClient(engine: HttpClientEngine, server: String, token: String?): HttpClient {
    return HttpClient(engine) {
        expectSuccess = true

        defaultRequest {
            url(server)
        }

        if (token != null) {
            install(Auth) {
                bearer {
                    loadTokens {
                        // TODO implement real bearer
                        BearerTokens(token, "not used")
                    }
                }
            }
        }

        install(Resources)
        install(ContentNegotiation) {
            gson()
        }
    }
}

class RecipeServiceWrapper {

    private var recipeService: RecipeService? = null

    suspend fun login(server: String, email: String, password: String): LoginState {
        val tmpService = RecipeService(createHttpClient(CIO.create(), server, null))

        // TODO Store the token, don't force authentication all the time
        val result = tmpService.login(LoginRequest(email, password))
        result.onSuccess { data ->
            recipeService = RecipeService(createHttpClient(CIO.create(), server, data.data.token))

            return LoginState.LoginSuccess
        }
        // TODO fix error handling
        result.onFailure {
            return LoginState.LoginFailure(R.string.unknown_error)
        }

        return LoginState.LoginFailure(R.string.unknown_error)
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

            result.onSuccess {
                Log.i("Recipes", "Created recipe at backend: $recipe")
                return true
            }
            result.onFailure {
                Log.i("Recipes", "Failed to create recipe on backend: $recipe")
                return false
            }
        }

        return false
    }

    suspend fun deleteRecipe(recipeId: Long): Boolean {
        Log.i("Recipes", "Deleting recipe from backend: $recipeId")
        // TODO make this safe by design

        recipeService?.apply {
            val result = deleteRecipe(
                recipeId = recipeId
            )

            result.onSuccess {
                Log.i("Recipes", "Deleted recipe from backend: $recipeId")
                return true
            }
            result.onFailure {
                Log.i("Recipes", "Failed to delete recipe from backend: $recipeId")
                return false
            }
        }

        return false
    }

    suspend fun sync(dao: RecipeDao) {
        recipeService?.apply {
            val existingRecipes = mutableSetOf<Long>()

            val maybeRecipes = getRecipes().getOrNull()
            maybeRecipes?.let { recipes ->

                for (r in recipes) {
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
}
