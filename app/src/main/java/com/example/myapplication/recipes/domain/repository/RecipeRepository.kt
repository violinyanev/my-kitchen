package com.example.myapplication.recipes.domain.repository

import com.example.myapplication.recipes.domain.model.Recipe
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    fun getRecipes(): Flow<List<Recipe>>

    suspend fun getRecipeById(id: Int): Recipe?

    suspend fun insertRecipe(recipe: Recipe)

    suspend fun deketeRecipe(recipe: Recipe)
}
