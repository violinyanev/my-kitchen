package com.example.myapplication.feature_recipe.domain.repository

import com.example.myapplication.feature_recipe.domain.model.Recipe
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    fun getRecipes(): Flow<List<Recipe>>

    suspend fun getRecipeById(id: Int): Recipe?

    suspend fun insertRecipe(recipe: Recipe)

    suspend fun deketeRecipe(recipe: Recipe)
}
