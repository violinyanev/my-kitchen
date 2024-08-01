package com.ultraviolince.mykitchen.recipes.data

import com.ultraviolince.mykitchen.recipes.data.datasource.localdb.RecipeDao
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeDao : RecipeDao {

    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())

    override fun getRecipes(): Flow<List<Recipe>> = _recipes

    override suspend fun getRecipeById(id: Long): Recipe? {
        return null
    }

    override suspend fun insertRecipe(recipe: Recipe): Long {
        val id = _recipes.value.size
        _recipes.update { currentRecipes ->
            currentRecipes + recipe
        }
        return id.toLong()
    }

    override suspend fun insertRecipes(recipes: List<Recipe>) {
        _recipes.update { currentRecipes ->
            currentRecipes + recipes
        }
    }

    override suspend fun deleteRecipe(recipe: Recipe) {
        _recipes.update { currentRecipes ->
            currentRecipes - recipe
        }
    }
}
