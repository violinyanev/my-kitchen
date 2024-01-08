package com.example.myapplication.recipes.presentation.recipes

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.recipes.domain.model.Recipe
import com.example.myapplication.recipes.domain.usecase.Recipes
import com.example.myapplication.recipes.domain.util.RecipeOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val recipesUseCases: Recipes
) : ViewModel() {

    private val _state = mutableStateOf(RecipesState())
    val state: State<RecipesState> = _state

    private var recentlyDeletedRecipe: Recipe? = null

    private var getRecipesJob: Job? = null

    init {
        getRecipes(RecipeOrder.Date(state.value.recipeOrder.orderType))
    }

    fun onEvent(event: RecipesEvent) {
        when (event) {
            is RecipesEvent.Order -> {
                // TODO implement
                if (state.value.recipeOrder::class == event.recipesOrder::class &&
                    state.value.recipeOrder.orderType == event.recipesOrder.orderType
                ) {
                    return
                }
                getRecipes(event.recipesOrder)
            }
            is RecipesEvent.RestoreRecipe -> {
                viewModelScope.launch {
                    recipesUseCases.addRecipe(recentlyDeletedRecipe ?: return@launch)
                    recentlyDeletedRecipe = null
                }
            }
            is RecipesEvent.DeleteRecipe -> {
                viewModelScope.launch {
                    recipesUseCases.deleteRecipe(event.recipe)
                    recentlyDeletedRecipe = event.recipe
                }
            }
            is RecipesEvent.ToggleOrderSection -> {
                _state.value = state.value.copy(
                    isOrderSelectionVisible =
                    !state.value.isOrderSelectionVisible
                )
            }
        }
    }

    private fun getRecipes(recipesOrder: RecipeOrder) {
        getRecipesJob?.cancel()
        getRecipesJob = recipesUseCases.getRecipes(recipesOrder)
            .onEach {
                    recipes ->
                _state.value = state.value.copy(
                    recipes = recipes,
                    recipeOrder = recipesOrder
                )
            }
            .launchIn(viewModelScope)
    }
}
