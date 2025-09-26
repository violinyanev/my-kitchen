package com.ultraviolince.mykitchen.recipes.data.repository

import com.ultraviolince.mykitchen.recipes.data.datasource.localdb.RecipeDao
import com.ultraviolince.mykitchen.recipes.data.datasource.localdb.entity.Recipe as LocalRecipe
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.repository.RecipeRepository
import com.ultraviolince.mykitchen.recipes.domain.service.RecipeNetworkService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RecipeRepositoryImpl(
    private val dao: RecipeDao,
    private val recipeNetworkService: RecipeNetworkService,
) : RecipeRepository {

    override fun getRecipes(): Flow<List<Recipe>> {
        return dao.getRecipes().map { localRecipes ->
            localRecipes.map { it.toSharedRecipe() }
        }
    }

    override suspend fun getRecipeById(id: Long): Recipe? {
        return dao.getRecipeById(id)?.toSharedRecipe()
    }

    override suspend fun insertRecipe(recipe: Recipe): Long {
        val localRecipe = LocalRecipe.fromSharedRecipe(recipe)
        val recipeId = dao.insertRecipe(localRecipe)
        recipeNetworkService.insertRecipe(recipeId, recipe)
        return recipeId
    }

    override suspend fun deleteRecipe(recipe: Recipe) {
        recipe.id?.let { id ->
            val localRecipe = LocalRecipe.fromSharedRecipe(recipe)
            recipeNetworkService.deleteRecipe(id)
            dao.deleteRecipe(localRecipe)
        }
    }
}
