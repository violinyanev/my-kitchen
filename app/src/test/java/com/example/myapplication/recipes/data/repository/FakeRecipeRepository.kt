package com.example.myapplication.recipes.data.repository

import com.example.myapplication.recipes.domain.model.Recipe
import com.example.myapplication.recipes.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking

class FakeRecipeRepository : RecipeRepository {

    private val recipes = mutableListOf<Recipe>()

    init {
        val recipesToInsert = mutableListOf<Recipe>()
        ('a'..'z').forEachIndexed { index, c ->
            recipesToInsert.add(
                Recipe(
                    title = c.toString(),
                    content = c.toString(),
                    timestamp = index.toLong()
                )
            )
        }
        recipesToInsert.shuffle()
        runBlocking {
            recipesToInsert.forEach { insertRecipe(it) }
        }
    }

    override fun getRecipes(): Flow<List<Recipe>> {
        return flow { emit(recipes) }
    }

    fun getTestRecipes(): List<Recipe> {
        return recipes
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
