package com.example.myapplication.recipes.data.repository

import android.util.Log
import com.example.myapplication.recipes.data.datasource.backend.RecipeServiceWrapper
import com.example.myapplication.recipes.data.datasource.localdb.RecipeDao
import com.example.myapplication.recipes.domain.model.Recipe
import com.example.myapplication.recipes.domain.repository.RecipeRepository
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

// TODO Split into API code
@OptIn(DelicateCoroutinesApi::class)
class RecipeRepositoryImpl(
    private val dao: RecipeDao,
    private val recipeService: RecipeServiceWrapper
) : RecipeRepository {
    override fun getRecipes(): Flow<List<Recipe>> {
        return dao.getRecipes()
    }

    init {
        // TODO improve
        GlobalScope.launch {
            recipeService.syncToDao(dao)
        }
    }

    override suspend fun getRecipeById(id: Long): Recipe? {
        return dao.getRecipeById(id)
    }

    override suspend fun insertRecipe(recipe: Recipe): Long {
        val recipeId = dao.insertRecipe(recipe)
        // TODO remove log
        Log.e("RECIPES", "Recipe id: $recipeId")
        recipeService.insertRecipe(recipeId, recipe)
        return recipeId
    }

    override suspend fun deleteRecipe(recipe: Recipe) {
        return dao.deleteRecipe(recipe)
    }
}
