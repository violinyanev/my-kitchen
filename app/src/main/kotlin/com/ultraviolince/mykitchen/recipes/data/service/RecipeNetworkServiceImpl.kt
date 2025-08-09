package com.ultraviolince.mykitchen.recipes.data.service

import android.util.Log
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.RecipeService
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.data.BackendRecipe
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.util.Result
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.util.onSuccess
import com.ultraviolince.mykitchen.recipes.data.datasource.datastore.SafeDataStore
import com.ultraviolince.mykitchen.recipes.data.datasource.localdb.RecipeDao
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.service.NetworkService
import com.ultraviolince.mykitchen.recipes.domain.service.RecipeNetworkService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

@Single
class RecipeNetworkServiceImpl(
    private val dataStore: SafeDataStore,
    private val dao: RecipeDao,
    private val networkService: NetworkService
) : RecipeNetworkService {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var recipeService: RecipeService? = null

    init {
        // Initialize service if credentials are available
        scope.launch {
            val prefs = dataStore.preferences.first()
            
            if (prefs.server != null && prefs.token != null) {
                Log.d("#recipe_network", "Initializing recipe service with stored credentials")
                recipeService = RecipeService(networkService.createHttpClient(prefs.server, prefs.token))
            }
        }
    }

    override suspend fun insertRecipe(recipeId: Long, recipe: Recipe): Boolean {
        Log.i("#recipe_network", "Syncing recipe to backend: $recipe")

        return recipeService?.let { service ->
            val result = service.createRecipe(
                BackendRecipe(
                    id = recipeId,
                    title = recipe.title,
                    body = recipe.content,
                    timestamp = recipe.timestamp
                )
            )

            Log.i("#recipe_network", "Create recipe result: $result")
            when (result) {
                is Result.Error -> false
                is Result.Success -> true
            }
        } ?: false
    }

    override suspend fun deleteRecipe(recipeId: Long): Boolean {
        Log.i("#recipe_network", "Deleting recipe from backend: $recipeId")
        
        return recipeService?.let { service ->
            val result = service.deleteRecipe(recipeId = recipeId)

            Log.i("#recipe_network", "Delete recipe result: $result")
            when (result) {
                is Result.Error -> false
                is Result.Success -> true
            }
        } ?: false
    }

    override suspend fun syncRecipes() {
        recipeService?.let { service ->
            val existingRecipes = mutableSetOf<Long>()

            val maybeRecipes = service.getRecipes()

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
        }
    }

    fun updateService() {
        scope.launch {
            val prefs = dataStore.preferences.first()
            
            recipeService = if (prefs.server != null && prefs.token != null) {
                Log.d("#recipe_network", "Updating recipe service with new credentials")
                RecipeService(networkService.createHttpClient(prefs.server, prefs.token))
            } else {
                Log.d("#recipe_network", "Clearing recipe service - no credentials")
                null
            }
        }
    }
}