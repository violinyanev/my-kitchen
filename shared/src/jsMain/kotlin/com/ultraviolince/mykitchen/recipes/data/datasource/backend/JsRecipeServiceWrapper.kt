package com.ultraviolince.mykitchen.recipes.data.datasource.backend

import com.ultraviolince.mykitchen.recipes.data.datasource.backend.data.BackendRecipe
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.data.LoginRequest
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.util.Result
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.util.onSuccess
import com.ultraviolince.mykitchen.recipes.data.datasource.datastore.LocalStorageDataStore
import com.ultraviolince.mykitchen.recipes.data.datasource.localstorage.LocalStorageRecipeDao
import com.ultraviolince.mykitchen.recipes.data.datasource.localstorage.entity.Recipe as LocalRecipe
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.logging.Logger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class JsRecipeServiceWrapper(private val dataStore: LocalStorageDataStore, private val dao: LocalStorageRecipeDao) {

    private var recipeService: RecipeService? = null

    val loginState = MutableStateFlow<LoginState>(LoginState.LoginEmpty)

    private val logger = object : Logger {
        override fun log(message: String) {
            console.log("#network #ktor #data: $message")
        }
    }

    init {
        // TODO is a better way possible?
        @Suppress("OPT_IN_USAGE")
        GlobalScope.launch {
            val prefs = dataStore.preferences.first()

            logger.log("Checking stored preferences")
            if (prefs.server != null && prefs.token != null) {
                logger.log("Restoring data, server=${prefs.server}")
                recipeService = RecipeService(createHttpClient(Js.create(), prefs.server, prefs.token, logger))

                // TODO: check if token still valid
                loginState.emit(LoginState.LoginSuccess)
            }
        }
    }

    suspend fun login(server: String, email: String, password: String) {
        loginState.emit(LoginState.LoginPending)

        val tmpService = RecipeService(createHttpClient(Js.create(), server, null, logger))

        // TODO wipe pref data?

        val result = tmpService.login(LoginRequest(email, password))

        console.log("#network: Login result: $result")
        when (result) {
            is Result.Error -> loginState.emit(LoginState.LoginFailure(error = result.error))
            is Result.Success -> {
                recipeService = RecipeService(createHttpClient(Js.create(), server, result.data.data.token, logger))
                dataStore.write(server = server, token = result.data.data.token)
                sync()
                loginState.emit(LoginState.LoginSuccess)
            }
        }
    }

    suspend fun logout() {
        dataStore.write("", "")
        loginState.emit(LoginState.LoginEmpty)
    }

    suspend fun insertRecipe(recipeId: Long, recipe: Recipe): Boolean {
        console.log("Syncing recipe to backend: $recipe")

        recipeService?.apply {
            val result = createRecipe(
                BackendRecipe(
                    id = recipeId,
                    title = recipe.title,
                    body = recipe.content,
                    timestamp = recipe.timestamp
                )
            )

            console.log("#network: Create recipe result: $result")
            return when (result) {
                is Result.Error -> false
                is Result.Success -> true
            }
        }

        return false
    }

    suspend fun deleteRecipe(recipeId: Long): Boolean {
        console.log("Deleting recipe from backend: $recipeId")
        // TODO make this safe by design

        recipeService?.apply {
            val result = deleteRecipe(
                recipeId = recipeId
            )

            console.log("#network: Delete recipe result: $result")
            return when (result) {
                is Result.Error -> false
                is Result.Success -> true
            }
        }

        return false
    }

    private suspend fun sync() {
        recipeService?.apply {
            val existingRecipes = mutableSetOf<Long>()

            val maybeRecipes = getRecipes()

            maybeRecipes.onSuccess { recipes ->
                for (r in recipes) {
                    dao.insertRecipe(
                        LocalRecipe(
                            id = r.id,
                            title = r.title,
                            content = r.body,
                            timestamp = r.timestamp
                        )
                    )
                    existingRecipes.add(r.id)
                }
            }
        }
    }
}