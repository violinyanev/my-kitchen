package com.ultraviolince.mykitchen.recipes.data.datasource.localstorage

import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import kotlinx.browser.localStorage
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

class LocalStorageRecipeDao {
    private val json = Json { ignoreUnknownKeys = true }
    private val recipesKey = "my_kitchen_recipes"
    
    fun getAll(): List<Recipe> {
        return try {
            val recipesJson = localStorage.getItem(recipesKey)
            if (recipesJson != null) {
                json.decodeFromString<List<Recipe>>(recipesJson)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            console.error("Error loading recipes from localStorage:", e)
            emptyList()
        }
    }
    
    fun insert(recipe: Recipe): Long {
        val recipes = getAll().toMutableList()
        val id = js("Date.now()") as Long // JS-specific ID generation
        val recipeWithId = recipe.copy(id = id)
        recipes.add(recipeWithId)
        saveAll(recipes)
        return id
    }
    
    fun delete(recipe: Recipe) {
        val recipes = getAll().toMutableList()
        recipes.removeAll { it.id == recipe.id }
        saveAll(recipes)
    }
    
    fun getById(id: Long): Recipe? {
        return getAll().find { it.id == id }
    }
    
    private fun saveAll(recipes: List<Recipe>) {
        try {
            val recipesJson = json.encodeToString(recipes)
            localStorage.setItem(recipesKey, recipesJson)
        } catch (e: Exception) {
            console.error("Error saving recipes to localStorage:", e)
        }
    }
}
