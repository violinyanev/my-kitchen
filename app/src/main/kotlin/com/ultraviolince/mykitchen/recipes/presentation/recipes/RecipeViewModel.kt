package com.ultraviolince.mykitchen.recipes.presentation.recipes

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.usecase.Recipes
import com.ultraviolince.mykitchen.recipes.domain.util.RecipeOrder
import com.ultraviolince.mykitchen.recipes.presentation.util.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class RecipeViewModel(
    private val recipesUseCases: Recipes
) : ViewModel(), BaseViewModel<RecipesEvent, RecipesState> {

    private val _state = mutableStateOf(RecipesState())
    val state: State<RecipesState> = _state

    private var recentlyDeletedRecipe: Recipe? = null

    private var getRecipesJob: Job? = null
    private var getLoginJob: Job? = null

    init {
        getRecipes(RecipeOrder.Date(state.value.recipeOrder.orderType))
        getLoginStatus()
    }

    override fun onEvent(event: RecipesEvent) {
        when (event) {
            is RecipesEvent.Order -> handleOrderEvent(event)
            is RecipesEvent.RestoreRecipe -> handleRestoreRecipe()
            is RecipesEvent.DeleteRecipe -> handleDeleteRecipe(event)
            is RecipesEvent.ToggleOrderSection -> handleToggleOrderSection()
        }
    }

    private fun handleOrderEvent(event: RecipesEvent.Order) {
        if (state.value.recipeOrder::class == event.recipesOrder::class &&
            state.value.recipeOrder.orderType == event.recipesOrder.orderType
        ) {
            return
        }
        getRecipes(event.recipesOrder)
    }

    private fun handleRestoreRecipe() {
        viewModelScope.launch {
            recipesUseCases.addRecipe(recentlyDeletedRecipe ?: return@launch)
            recentlyDeletedRecipe = null
        }
    }

    private fun handleDeleteRecipe(event: RecipesEvent.DeleteRecipe) {
        Log.i("Recipes", "User deleted recipe ${event.recipe}")
        viewModelScope.launch {
            recipesUseCases.deleteRecipe(event.recipe)
            recentlyDeletedRecipe = event.recipe
        }
    }

    private fun handleToggleOrderSection() {
        _state.value = state.value.copy(
            isOrderSelectionVisible = !state.value.isOrderSelectionVisible
        )
    }

    private fun getRecipes(recipesOrder: RecipeOrder) {
        getRecipesJob?.cancel()
        getRecipesJob = recipesUseCases.getRecipes(recipesOrder)
            .onEach { recipes ->
                _state.value = state.value.copy(
                    recipes = ImmutableRecipesList(recipes),
                    recipeOrder = recipesOrder
                )
            }
            .launchIn(viewModelScope)
    }

    private fun getLoginStatus() {
        getLoginJob?.cancel()
        getLoginJob = recipesUseCases.getSyncState()
            .onEach { syncState ->
                Log.i("Recipes", "Login state changed to $syncState")
                _state.value = state.value.copy(
                    syncState = syncState
                )
            }
            .launchIn(viewModelScope)
    }
}
