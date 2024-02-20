package com.ultraviolince.mykitchen.recipes.data.datasource.backend

import com.ultraviolince.mykitchen.R
import com.ultraviolince.mykitchen.recipes.data.datasource.localdb.RecipeDao
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RecipeServiceWrapper {

    private var recipeService: RecipeService? = null

    suspend fun login(server: String, email: String, password: String): LoginState {
        try {
            val tmpService = Retrofit.Builder()
                .baseUrl(server)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(RecipeService::class.java)

            // TODO Store the token, don't force authentication all the time
            val token = tmpService.login(LoginRequest(email, password)).data.token
            recipeService = Retrofit.Builder()
                .baseUrl(server)
                .client(OkHttpClient.Builder().addInterceptor(AuthInterceptor(token)).build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(RecipeService::class.java)
        } catch (e: java.lang.IllegalArgumentException) {
            return LoginState.LoginFailure(R.string.malformed_server_uri)
        } catch (e: HttpException) {
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
        recipeService?.createRecipe(
            BackendRecipe(
                id = recipeId,
                title = recipe.title,
                body = recipe.content,
                timestamp = recipe.timestamp
            )
        )
    }

    suspend fun deleteRecipe(recipeId: Long) {
        // TODO check response
        recipeService?.deleteRecipe(recipeId = recipeId)
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
