package com.ultraviolince.mykitchen.recipes.data.datasource.backend

import android.util.Log
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.data.BackendRecipe
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe

fun Recipe.toBackendRecipe(): BackendRecipe {
    return BackendRecipe(
        id = this.id!!,
        title = this.title,
        body = this.content,
        timestamp = this.timestamp
    )
}

fun BackendRecipe.toRecipe(): Recipe {
    return Recipe(
        id = this.id,
        title = this.title,
        content = this.body,
        timestamp = this.timestamp
    )
}

data class RecipesDiff(
    val localRecipes: List<Recipe>,
    val backendRecipes: List<BackendRecipe>
)

object RecipeMerger {
    fun getDiff(localRecipes: List<Recipe>, backendRecipes: List<BackendRecipe>): RecipesDiff {
        val recipesToUploadToBackend = mutableListOf<BackendRecipe>()
        val recipesToSaveToDb = mutableListOf<Recipe>()

        val backendRecipesMap = backendRecipes.associateBy { it.id }
        val localRecipesMap = localRecipes.associateBy { it.id }

        for (r in localRecipes) {
            Log.i("#sync", "Checking local recipe $r")
            if (r.id !in backendRecipesMap || r.timestamp > backendRecipesMap[r.id]!!.timestamp) {
                Log.i("#sync", "Recipe $r will be uploaded to the backend")
                recipesToUploadToBackend.add(r.toBackendRecipe())
            }
        }

        for (r in backendRecipes) {
            Log.i("#sync", "Checking backend recipe $r")
            if (r.id !in localRecipesMap || r.timestamp > localRecipesMap[r.id]!!.timestamp) {
                Log.i("#sync", "Recipe $r was restored from the backend")
                recipesToSaveToDb.add(r.toRecipe())
            }
        }

        return RecipesDiff(recipesToSaveToDb, recipesToUploadToBackend)
    }
}
