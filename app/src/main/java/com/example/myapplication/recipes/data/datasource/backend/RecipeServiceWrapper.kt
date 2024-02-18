package com.example.myapplication.recipes.data.datasource.backend

import com.example.myapplication.recipes.domain.model.Recipe
import com.example.myapplication.recipes.domain.repository.LoginState
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
            // TODO internationalize
            return LoginState.LoginFailure("Malformed server URL! Use http(s)://yourdomain.com(:port)")
        } catch (e: HttpException) {
            return LoginState.LoginFailure("Bad credentials!")
        }

        return if (recipeService != null) {
            LoginState.LoginSuccess
        } else {
            LoginState.LoginFailure("Unknown error")
        }
    }

    // TODO fix this
    private suspend fun sync() {
        /*runBlocking {
            launch(Dispatchers.IO) {
                val response = recipeService?.getRecipes()?.execute()

                if (response?.isSuccessful == true) {
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
                        Log.e("RECIPES", "General API failure: " + response.message())
                        throw BackendException(customMessage = "General API failure: " + response.message())
                    }
                } else {
                    // TODO improve handling here
                    Log.e("RECIPES", "General API failure: " + response?.message())
                    throw BackendException(customMessage = "General API failure: " + response?.message())
                }

                val dbRecipes = dao.getRecipes()

                dbRecipes.collectLatest {
                    for (r in it.iterator()) {
                        insertRecipe(r.id!!, r)
                    }
                }
            }
        }*/
    }

    fun insertRecipe(recipeId: Long, recipe: Recipe) {
        // TODO re-enable after auth is fixed
        /*recipeService.createRecipe(
            BackendRecipe(
                id = recipeId,
                title = recipe.title,
                body = recipe.content,
                timestamp = recipe.timestamp
            )
        )?.enqueue(
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
        )*/
    }
}
