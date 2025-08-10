package com.ultraviolince.mykitchen.recipes.domain.service

import com.ultraviolince.mykitchen.recipes.domain.model.Recipe

interface RecipeNetworkService {
    suspend fun insertRecipe(recipeId: Long, recipe: Recipe): Boolean
    suspend fun deleteRecipe(recipeId: Long): Boolean
    suspend fun syncRecipes()
}