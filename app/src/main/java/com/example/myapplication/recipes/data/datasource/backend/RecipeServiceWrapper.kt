package com.example.myapplication.recipes.data.datasource.backend

import android.util.Log
import com.example.myapplication.recipes.data.datasource.localdb.RecipeDao
import com.example.myapplication.recipes.domain.model.Recipe
import com.example.myapplication.recipes.domain.repository.LoginState
import dagger.Provides
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

class RecipeServiceWrapper (private val dao: RecipeDao) {

    private val loginState = MutableStateFlow<LoginState>(LoginState.LoginEmpty)
    private var recipeService: RecipeService? = null
    private val token: String? = null

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun login(server: String, username: String, password: String) {

        recipeService = Retrofit.Builder()
                    .baseUrl(server)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(RecipeService::class.java)

        recipeService?.let {
            // TODO improve
            GlobalScope.launch {
                getToken(username, password)
            }

            // TODO test that authentication works
            //loginState.emit(LoginState.LoginSuccess)
            // TODO update sync status
        }
    }

    fun getLoginState(): Flow<LoginState> {
        return loginState
    }

    // TODO fix this
    private suspend fun getToken(username: String, password: String) {
        runBlocking {
            launch(Dispatchers.IO) {

                recipeService?.login(
                    LoginRequest(
                        email = username,
                        password = password,
                    )
                )?.enqueue(
                    object :
                        Callback<LoginResult> {
                        override fun onResponse(
                            call: Call<LoginResult>,
                            response: Response<LoginResult>
                        ) {

                            if (response.isSuccessful) {
                                response.body()?.let {
                                    Log.e("RECIPES", "Got the token: " + it.token)
                                } ?: {
                                    // TODO improve handling here
                                    Log.e("RECIPES", "General API failure: " + response.message())
                                    throw BackendException(customMessage = "General API failure: " + response.message())
                                }
                            } else {
                                // TODO improve handling here
                                Log.e("RECIPES", "General API failure: " + response.message())
                                throw BackendException(customMessage = "General API failure: " + response.message())
                            }
                        }

                        override fun onFailure(
                            call: Call<LoginResult>,
                            t: Throwable
                        ) {
                            // TODO proper error handling
                            Log.d("RECIPES", "Error getting auth token!! ")
                        }
                    }
                )
            }
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
