package com.ultraviolince.mykitchen.recipes.data.datasource.localstorage

import com.ultraviolince.mykitchen.recipes.data.datasource.localstorage.entity.Recipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.browser.localStorage

/**
 * JavaScript/Web implementation of RecipeDao using localStorage
 */
class LocalStorageRecipeDao {
    private companion object {
        private const val RECIPES_KEY = "my_kitchen_recipes"
    }

    private val _recipesFlow = MutableStateFlow<List<Recipe>>(emptyList())
    private val json = Json { ignoreUnknownKeys = true }

    init {
        loadRecipesFromStorage()
    }

    fun getRecipes(): Flow<List<Recipe>> = _recipesFlow.asStateFlow()

    suspend fun getRecipeById(id: Long): Recipe? {
        return _recipesFlow.value.find { it.id == id }
    }

    suspend fun insertRecipe(recipe: Recipe): Long {
        val recipes = _recipesFlow.value.toMutableList()
        
        // Generate ID if not present
        val newRecipe = if (recipe.id == null) {
            val newId = (recipes.maxOfOrNull { it.id ?: 0L } ?: 0L) + 1
            recipe.copy(id = newId)
        } else {
            // Update existing or add new
            recipes.removeAll { it.id == recipe.id }
            recipe
        }
        
        recipes.add(newRecipe)
        recipes.sortByDescending { it.timestamp }
        
        saveRecipesToStorage(recipes)
        _recipesFlow.value = recipes
        
        return newRecipe.id!!
    }

    suspend fun insertRecipes(recipes: List<Recipe>) {
        val currentRecipes = _recipesFlow.value.toMutableList()
        
        for (recipe in recipes) {
            currentRecipes.removeAll { it.id == recipe.id }
            if (recipe.id != null) {
                currentRecipes.add(recipe)
            }
        }
        
        currentRecipes.sortByDescending { it.timestamp }
        saveRecipesToStorage(currentRecipes)
        _recipesFlow.value = currentRecipes
    }

    suspend fun deleteRecipe(recipe: Recipe) {
        val recipes = _recipesFlow.value.toMutableList()
        recipes.removeAll { it.id == recipe.id }
        
        saveRecipesToStorage(recipes)
        _recipesFlow.value = recipes
    }

    private fun loadRecipesFromStorage() {
        try {
            val recipesJson = localStorage.getItem(RECIPES_KEY)
            if (recipesJson != null) {
                val recipes = json.decodeFromString<List<Recipe>>(recipesJson)
                _recipesFlow.value = recipes
            }
        } catch (e: Exception) {
            console.error("Failed to load recipes from localStorage:", e)
            _recipesFlow.value = emptyList()
        }
    }

    private fun saveRecipesToStorage(recipes: List<Recipe>) {
        try {
            val recipesJson = json.encodeToString(recipes)
            localStorage.setItem(RECIPES_KEY, recipesJson)
        } catch (e: Exception) {
            console.error("Failed to save recipes to localStorage:", e)
        }
    }
}