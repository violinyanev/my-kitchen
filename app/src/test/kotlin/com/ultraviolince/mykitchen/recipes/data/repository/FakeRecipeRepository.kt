package com.ultraviolince.mykitchen.recipes.data.repository

import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.repository.CloudSyncState
import com.ultraviolince.mykitchen.recipes.domain.repository.RecipeRepository
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
                    timestamp = index.toLong(),
                    id = index.toLong()
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

    override suspend fun getRecipeById(id: Long): Recipe? {
        return recipes.find { it.id == id }
    }

    override suspend fun insertRecipe(recipe: Recipe): Long {
        recipes.add(recipe)
        return recipes.size.toLong()
    }

    override suspend fun deleteRecipe(recipe: Recipe) {
        recipes.remove(recipe)
    }

    override suspend fun login(server: String, email: String, password: String) {
    }

    override fun getLoginState(): Flow<CloudSyncState> {
        return flow {
            emit(CloudSyncState.NotLoggedIn)
        }
    }
}
