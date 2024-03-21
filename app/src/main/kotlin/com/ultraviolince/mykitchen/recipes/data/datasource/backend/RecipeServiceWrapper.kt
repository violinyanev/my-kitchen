package com.ultraviolince.mykitchen.recipes.data.datasource.backend

import android.util.Log
import com.ultraviolince.mykitchen.R
import com.ultraviolince.mykitchen.recipes.data.datasource.localdb.RecipeDao
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.model.User
import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RecipeServiceWrapper {

    private var recipeService: RecipeService? = null

    suspend fun login(user: User, password: String?): LoginState {
        try {
            val tmpService = Retrofit.Builder()
                .baseUrl(user.serverUri)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(RecipeService::class.java)

            Log.e("Recipes", "User: $user")
            val token = user.token ?: tmpService.login(LoginRequest(email = user.email, password = password!!)).data.token

            val logger = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            recipeService = Retrofit.Builder()
                .baseUrl(user.serverUri)
                .client(OkHttpClient.Builder().addInterceptor(AuthInterceptor(token)).addInterceptor(logger).build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(RecipeService::class.java)
        } catch (e: java.lang.IllegalArgumentException) {
            return LoginState.LoginFailure(R.string.malformed_server_uri)
        } catch (e: HttpException) {
            Log.e("Recipes", "Wrong credentials: ${e.message}")
            return LoginState.LoginFailure(R.string.wrong_credentials)
        }

        return if (recipeService != null) {
            LoginState.LoginSuccess
        } else {
            LoginState.LoginFailure(R.string.unknown_error)
        }
    }

    suspend fun insertRecipe(recipeId: Long, recipe: Recipe) {
        // TODO check response
        Log.i("Recipes", "Syncing recipe to backend: $recipe")
        try {
            recipeService?.createRecipe(
                BackendRecipe(
                    id = recipeId,
                    title = recipe.title,
                    body = recipe.content,
                    timestamp = recipe.timestamp
                )
            )
        } catch (e: HttpException) {
            // TODO better error handling
            Log.e("Recipes", "Failed to sync recipe $recipe. Reason: ${e.message()} req: ${e.response()}")
        }
        Log.i("Recipes", "Recipe synced to backend: $recipe")
    }

    suspend fun deleteRecipe(recipeId: Long) {
        Log.i("Recipes", "Deleting recipe from backend: $recipeId")
        try {
            // TODO check response
            recipeService?.deleteRecipe(recipeId = recipeId)
        } catch (e: HttpException) {
            // TODO better error handling
            Log.e("Recipes", "Failed to delete recipe $recipeId. Reason: ${e.message()} req: ${e.response()}")
        }
        Log.i("Recipes", "Deleted recipe from backend: $recipeId")
    }

    suspend fun sync(dao: RecipeDao) {
        recipeService?.let { service ->
            val existingRecipes = mutableSetOf<Long>()

            for (r in service.getRecipes()) {
                dao.insertRecipe(
                    Recipe(
                        id = r.id,
                        title = r.title,
                        content = r.body,
                        timestamp = r.timestamp
                    )
                )
                existingRecipes.add(r.id)
            }

            /*val dbRecipes = dao.getRecipes()

            Log.e("RECIPES", "Before1")
            val currentDbRecipes = dbRecipes.last()

            Log.e("RECIPES", "Before2")

            for (r in currentDbRecipes) {
                r.id?.let {
                    if(!existingRecipes.contains(it)){
                        insertRecipe(it, r)
                    }
                }
            }
            Log.e("RECIPES", "After")*/
        }
    }
}
