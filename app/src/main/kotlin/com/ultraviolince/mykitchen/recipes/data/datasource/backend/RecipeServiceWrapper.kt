package com.ultraviolince.mykitchen.recipes.data.datasource.backend

import android.util.Log
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.data.BackendRecipe
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.data.LoginRequest
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.util.Result
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.util.onSuccess
import com.ultraviolince.mykitchen.recipes.data.datasource.datastore.SafeDataStore
import com.ultraviolince.mykitchen.recipes.data.datasource.localdb.RecipeDao
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.logging.Logger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RecipeServiceWrapper(
    private val dataStore: SafeDataStore,
    private val dao: RecipeDao,
    private val clientEngineProvider: () -> HttpClientEngine = { CIO.create() }
) {

    private var recipeService: RecipeService? = null

    val loginState = MutableStateFlow<LoginState>(LoginState.LoginEmpty)

    private val logger = object : Logger {
        override fun log(message: String) {
            Log.d("#network #ktor #data", message)
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
                recipeService = RecipeService(createHttpClient(clientEngineProvider(), prefs.server, prefs.token, logger))

                // TODO: check if token still valid
                loginState.emit(LoginState.LoginSuccess)
            }
        }
    }

    suspend fun login(server: String, email: String, password: String) {
        loginState.emit(LoginState.LoginPending)

        val tmpService = RecipeService(createHttpClient(clientEngineProvider(), server, null, logger))

        // TODO wipe pref data?

        val result = tmpService.login(LoginRequest(email, password))

        Log.i("#network", "Login result: $result")
        when (result) {
            is Result.Error -> loginState.emit(LoginState.LoginFailure(error = result.error))
            is Result.Success -> {
                recipeService = RecipeService(createHttpClient(clientEngineProvider(), server, result.data.data.token, logger))
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

            Log.i("#network", "Delete recipe result: $result")
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
