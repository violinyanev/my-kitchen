package com.ultraviolince.mykitchen.recipes.data.datasource.backend

import android.util.Log
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.data.BackendRecipe
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.data.LoginRequest
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.util.Result
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.util.onSuccess
import com.ultraviolince.mykitchen.recipes.data.datasource.datastore.SafeDataStore
import com.ultraviolince.mykitchen.recipes.data.datasource.localdb.RecipeDao
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.model.SyncStatus
import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.logging.Logger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RecipeServiceWrapper(private val dataStore: SafeDataStore, private val dao: RecipeDao) {

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
                recipeService = RecipeService(createHttpClient(CIO.create(), prefs.server, prefs.token, logger))

                // TODO: check if token still valid
                loginState.emit(LoginState.LoginSuccess)

                // Trigger comprehensive sync on app start when already logged in
                syncAllRecipes()
            }
        }
    }

    suspend fun login(server: String, email: String, password: String) {
        loginState.emit(LoginState.LoginPending)

        val tmpService = RecipeService(createHttpClient(CIO.create(), server, null, logger))

        // TODO wipe pref data?

        val result = tmpService.login(LoginRequest(email, password))

        Log.i("#network", "Login result: $result")
        when (result) {
            is Result.Error -> loginState.emit(LoginState.LoginFailure(error = result.error))
            is Result.Success -> {
                recipeService = RecipeService(createHttpClient(CIO.create(), server, result.data.data.token, logger))
                dataStore.write(server = server, token = result.data.data.token)
                syncAllRecipes() // Use comprehensive sync instead of simple sync
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
                            timestamp = r.timestamp,
                            syncStatus = SyncStatus.SYNCED,
                            lastSyncTimestamp = System.currentTimeMillis()
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

    suspend fun syncAllRecipes() {
        Log.i("Recipes", "Starting comprehensive sync of all recipes")

        val service = recipeService
        if (service == null) {
            Log.w("Recipes", "Cannot sync - not logged in")
            return
        }

        try {
            performSync(service)
        } catch (e: Exception) {
            handleSyncError(e)
        }
    }

    private suspend fun performSync(service: RecipeService) {
        val serverRecipesResult = service.getRecipes()

        serverRecipesResult.onSuccess { serverRecipes ->
            Log.i("Recipes", "Got ${serverRecipes.size} recipes from server")

            val serverRecipesMap = serverRecipes.associateBy { it.id }
            val localRecipes = getLocalRecipesForSync()

            Log.i("Recipes", "Got ${localRecipes.size} local recipes")

            processServerRecipes(serverRecipes, localRecipes)
            processLocalRecipes(localRecipes, serverRecipesMap)

            Log.i("Recipes", "Comprehensive sync completed successfully")
        }
    }

    private suspend fun getLocalRecipesForSync(): List<Recipe> {
        return dao.getRecipesBySyncStatuses(listOf(
            SyncStatus.NOT_SYNCED,
            SyncStatus.SYNCED,
            SyncStatus.SYNC_ERROR
        ))
    }

    private suspend fun processServerRecipes(serverRecipes: List<BackendRecipe>, localRecipes: List<Recipe>) {
        for (serverRecipe in serverRecipes) {
            val localRecipe = localRecipes.find { it.id == serverRecipe.id }

            when {
                localRecipe == null -> addRecipeFromServer(serverRecipe)
                serverRecipe.timestamp > localRecipe.timestamp -> updateLocalWithServer(serverRecipe, localRecipe)
                localRecipe.syncStatus != SyncStatus.SYNCED -> markAsSynced(localRecipe)
            }
        }
    }

    private suspend fun addRecipeFromServer(serverRecipe: BackendRecipe) {
        Log.i("Recipes", "Adding recipe from server: ${serverRecipe.title}")
        dao.insertRecipe(
            Recipe(
                id = serverRecipe.id,
                title = serverRecipe.title,
                content = serverRecipe.body,
                timestamp = serverRecipe.timestamp,
                syncStatus = SyncStatus.SYNCED,
                lastSyncTimestamp = System.currentTimeMillis()
            )
        )
    }

    private suspend fun updateLocalWithServer(serverRecipe: BackendRecipe, localRecipe: Recipe) {
        Log.i("Recipes", "Updating local recipe with server version: ${serverRecipe.title}")
        dao.updateRecipe(
            localRecipe.copy(
                title = serverRecipe.title,
                content = serverRecipe.body,
                timestamp = serverRecipe.timestamp,
                syncStatus = SyncStatus.SYNCED,
                lastSyncTimestamp = System.currentTimeMillis(),
                syncErrorMessage = null
            )
        )
    }

    private suspend fun markAsSynced(localRecipe: Recipe) {
        dao.updateRecipeSyncStatus(
            localRecipe.id!!,
            SyncStatus.SYNCED,
            System.currentTimeMillis()
        )
    }

    private suspend fun processLocalRecipes(localRecipes: List<Recipe>, serverRecipesMap: Map<Long, BackendRecipe>) {
        for (localRecipe in localRecipes) {
            if (localRecipe.syncStatus == SyncStatus.NOT_SYNCED &&
                !serverRecipesMap.containsKey(localRecipe.id)) {
                Log.i("Recipes", "Uploading local recipe to server: ${localRecipe.title}")
                localRecipe.id?.let { syncRecipeToServer(it, localRecipe) }
            }
        }
    }

    private suspend fun handleSyncError(e: Exception) {
        Log.e("Recipes", "Error during comprehensive sync", e)
        val syncingRecipes = dao.getRecipesBySyncStatus(SyncStatus.SYNCING)
        syncingRecipes.forEach { recipe ->
            recipe.id?.let {
                dao.updateRecipeSyncStatus(
                    it,
                    SyncStatus.SYNC_ERROR,
                    syncErrorMessage = "Sync failed: ${e.message}"
                )
            }
        }
    }

    private suspend fun syncRecipeToServer(recipeId: Long, recipe: Recipe): Boolean {
        dao.updateRecipeSyncStatus(recipeId, SyncStatus.SYNCING, System.currentTimeMillis())

        val success = insertRecipe(recipeId, recipe)

        if (success) {
            dao.updateRecipeSyncStatus(recipeId, SyncStatus.SYNCED, System.currentTimeMillis())
        } else {
            dao.updateRecipeSyncStatus(
                recipeId,
                SyncStatus.SYNC_ERROR,
                syncErrorMessage = "Failed to upload to server"
            )
        }

        return success
    }
}
