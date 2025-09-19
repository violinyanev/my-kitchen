package com.ultraviolince.mykitchen.recipes.presentation.recipes

import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
import com.ultraviolince.mykitchen.recipes.domain.usecase.Recipes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

/**
 * Shared recipes logic that can be used by both Android and Web platforms
 * This replaces the Android-specific RecipeViewModel with a platform-agnostic implementation
 */
class SharedRecipesManager(private val recipes: Recipes) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // Combined state for easy consumption by UI
    val recipesUiState: StateFlow<RecipesUiState> = combine(
        recipes.getRecipes(), 
        recipes.getSyncState()
    ) { recipesList: List<Recipe>, syncState ->
        RecipesUiState(
            recipes = recipesList,
            isLoggedIn = syncState is LoginState.LoginSuccess,
            syncStatus = when (syncState) {
                is LoginState.LoginEmpty -> "Not connected"
                is LoginState.LoginPending -> "Connecting..."
                is LoginState.LoginSuccess -> "✅ Connected & Synced"
                is LoginState.LoginFailure -> "❌ Connection Failed"
            }
        )
    }.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RecipesUiState(
            recipes = emptyList(),
            isLoggedIn = false,
            syncStatus = "Not connected"
        )
    )
    
    fun deleteRecipe(recipe: Recipe) {
        scope.launch {
            recipes.deleteRecipe(recipe)
        }
    }
}

data class RecipesUiState(
    val recipes: List<Recipe>,
    val isLoggedIn: Boolean,
    val syncStatus: String
)
