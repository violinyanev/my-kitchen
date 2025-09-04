package com.ultraviolince.mykitchen.recipes.presentation

import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
import com.ultraviolince.mykitchen.recipes.domain.usecase.Recipes
import com.ultraviolince.mykitchen.recipes.domain.util.OrderType
import com.ultraviolince.mykitchen.recipes.domain.util.RecipeOrder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Web-compatible state manager for recipes functionality
 * (Alternative to Android ViewModels)
 */
class RecipeStateManager(
    private val recipesUseCases: Recipes,
    private val scope: CoroutineScope
) {

    private val _recipesState = MutableStateFlow<List<Recipe>>(emptyList())
    val recipesState: StateFlow<List<Recipe>> = _recipesState.asStateFlow()
    
    private val _loginState = MutableStateFlow<LoginState>(LoginState.LoginEmpty)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var recentlyDeletedRecipe: Recipe? = null
    private var getRecipesJob: Job? = null
    private var getLoginJob: Job? = null

    init {
        getRecipes(RecipeOrder.Date(OrderType.Descending))
        getLoginStatus()
    }

    fun login(server: String, username: String, password: String) {
        scope.launch {
            _isLoading.value = true
            try {
                recipesUseCases.login(server, username, password)
            } catch (e: Exception) {
                console.error("Login failed:", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        scope.launch {
            recipesUseCases.logout()
        }
    }

    fun addRecipe(title: String, content: String) {
        scope.launch {
            _isLoading.value = true
            try {
                val recipe = Recipe(
                    title = title,
                    content = content,
                    timestamp = kotlinx.datetime.Clock.System.now().epochSeconds * 1000, // Convert to milliseconds
                    id = null
                )
                recipesUseCases.addRecipe(recipe)
            } catch (e: Exception) {
                console.error("Failed to add recipe:", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteRecipe(recipe: Recipe) {
        scope.launch {
            _isLoading.value = true
            try {
                recipesUseCases.deleteRecipe(recipe)
                recentlyDeletedRecipe = recipe
            } catch (e: Exception) {
                console.error("Failed to delete recipe:", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun restoreRecipe() {
        scope.launch {
            _isLoading.value = true
            try {
                val recipe = recentlyDeletedRecipe
                if (recipe != null) {
                    recipesUseCases.addRecipe(recipe)
                    recentlyDeletedRecipe = null
                }
            } catch (e: Exception) {
                console.error("Failed to restore recipe:", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun getRecipes(recipesOrder: RecipeOrder) {
        getRecipesJob?.cancel()
        getRecipesJob = recipesUseCases.getRecipes(recipesOrder)
            .onEach { recipes ->
                _recipesState.value = recipes
            }
            .launchIn(scope)
    }

    private fun getLoginStatus() {
        getLoginJob?.cancel()
        getLoginJob = recipesUseCases.getSyncState()
            .onEach { syncState ->
                console.log("Login state changed to $syncState")
                _loginState.value = syncState
            }
            .launchIn(scope)
    }
}