package com.example.myapplication.recipes.data.datasource.backend

import android.util.Log
import com.example.myapplication.recipes.data.datasource.localdb.RecipeDao
import com.example.myapplication.recipes.domain.model.Recipe
import kotlinx.coroutines.runBlocking
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecipeServiceWrapper(private val recipeService: RecipeService) {
    // TODO fix this
    fun syncToDao(dao: RecipeDao) {
        recipeService.getRecipes().enqueue(object :
            Callback<List<BackendRecipe>> {
            override fun onResponse(
                call: Call<List<BackendRecipe>>,
                response: Response<List<BackendRecipe>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        for (r in it.iterator()) {
                            runBlocking {
                                dao.insertRecipe(
                                    Recipe(
                                        id = r.id,
                                        title = r.title,
                                        content = r.body,
                                        timestamp = r.timestamp
                                    )
                                )
                            }
                        }
                    } ?: {
                        // TODO improve handling here
                        throw Exception("Empty body")
                    }

                } else {
                    // TODO improve handling here
                    throw Exception("Failed web request")
                }
            }

            override fun onFailure(
                call: Call<List<BackendRecipe>>,
                t: Throwable
            ) {
                // TODO improve handling here
                throw Exception("Network error :( ${t.message}")
            }
        })
    }

    fun insertRecipe(recipeId: Long, recipe: Recipe) {
        recipeService.createRecipe(
            BackendRecipe(
                id = recipeId,
                title = recipe.title,
                body = recipe.content,
                timestamp = recipe.timestamp
            )
        ).enqueue(
            object :
                Callback<BackendRecipe> {
                override fun onResponse(
                    call: Call<BackendRecipe>,
                    response: Response<BackendRecipe>
                ) {
                    // TODO affect synced status
                    Log.d("RECIPES", "Recipe created! ")
                }

                override fun onFailure(
                    call: Call<BackendRecipe>,
                    t: Throwable
                ) {
                    // TODO proper error handling
                    Log.d("RECIPES", "Error creating recipe!! ")
                }
            }
        )
    }
}
