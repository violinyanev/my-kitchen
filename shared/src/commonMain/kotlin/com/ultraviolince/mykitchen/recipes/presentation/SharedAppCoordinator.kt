package com.ultraviolince.mykitchen.recipes.presentation

import com.ultraviolince.mykitchen.recipes.domain.usecase.Recipes
import com.ultraviolince.mykitchen.recipes.presentation.addrecipe.SharedAddRecipeManager
import com.ultraviolince.mykitchen.recipes.presentation.login.SharedLoginManager
import com.ultraviolince.mykitchen.recipes.presentation.recipes.SharedRecipesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppScreen {
    LOGIN, RECIPES, ADD_RECIPE
}

/**
 * Shared app coordinator that manages navigation and provides access to all screen managers
 * This is the single source of truth for the application state and navigation
 * Both Android and Web platforms use this exact same coordinator
 */
class SharedAppCoordinator(private val recipes: Recipes) {
    
    // Screen managers - these contain the exact same business logic as Android ViewModels
    val loginManager = SharedLoginManager(recipes)
    val recipesManager = SharedRecipesManager(recipes)
    val addRecipeManager = SharedAddRecipeManager(recipes)
    
    // Navigation state
    private val _currentScreen = MutableStateFlow(AppScreen.LOGIN)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()
    
    fun navigateToLogin() {
        _currentScreen.value = AppScreen.LOGIN
    }
    
    fun navigateToRecipes() {
        _currentScreen.value = AppScreen.RECIPES
    }
    
    fun navigateToAddRecipe() {
        _currentScreen.value = AppScreen.ADD_RECIPE
        addRecipeManager.clearForm()
    }
    
    fun saveRecipeAndNavigateBack() {
        addRecipeManager.saveRecipe {
            navigateToRecipes()
        }
    }
}
