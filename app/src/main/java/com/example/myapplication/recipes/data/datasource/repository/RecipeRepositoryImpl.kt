package com.example.myapplication.recipes.data.datasource.repository

import com.example.myapplication.recipes.data.datasource.remote.RecipeAPI
import com.example.myapplication.recipes.domain.model.Recipe
import com.example.myapplication.recipes.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow

class RecipeRepositoryImpl (
    private val api: RecipeAPI
) : RecipeRepository {
    override fun getRecipes(): Flow<List<Recipe>> {
        TODO("Not yet implemented")
    }

    override suspend fun getRecipeById(id: Int): Recipe? {
        TODO("Not yet implemented")
    }

    override suspend fun insertRecipe(recipe: Recipe) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteRecipe(recipe: Recipe) {
        TODO("Not yet implemented")
    }
}