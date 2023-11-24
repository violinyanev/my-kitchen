package com.example.myapplication.recipes.data.repository

import com.example.myapplication.recipes.domain.model.Recipe
import com.example.myapplication.recipes.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeRecipeRepository : RecipeRepository {

    private val recipes = mutableListOf<Recipe>()

    override fun getRecipes(): Flow<List<Recipe>> {
        return flow { emit(recipes) }
    }

    override suspend fun getRecipeById(id: Int): Recipe? {
        return recipes.find { it.id == id }
    }

    override suspend fun insertRecipe(recipe: Recipe) {
        recipes.add(recipe)
    }

    override suspend fun deleteRecipe(recipe: Recipe) {
        recipes.remove(recipe)
    }
}
