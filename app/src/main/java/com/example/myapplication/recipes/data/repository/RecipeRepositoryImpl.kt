package com.example.myapplication.recipes.data.repository

import com.example.myapplication.recipes.data.datasource.RecipeDao
import com.example.myapplication.recipes.domain.model.Recipe
import com.example.myapplication.recipes.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow

class RecipeRepositoryImpl(
    private val dao: RecipeDao
) : RecipeRepository {
    override fun getRecipes(): Flow<List<Recipe>> {
        return dao.getRecipes()
    }

    override suspend fun getRecipeById(id: Int): Recipe? {
        return dao.getRecipeById(id)
    }

    override suspend fun insertRecipe(recipe: Recipe) {
        return dao.insertRecipe(recipe)
    }

    override suspend fun deleteRecipe(recipe: Recipe) {
        return dao.deleteRecipe(recipe)
    }
}
