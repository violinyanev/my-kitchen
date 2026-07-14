package com.ultraviolince.mykitchen.data.repository

import com.ultraviolince.mykitchen.data.local.RecipeDao
import com.ultraviolince.mykitchen.data.remote.RecipeApiClient
import com.ultraviolince.mykitchen.data.remote.dto.toDomain
import com.ultraviolince.mykitchen.data.store.CredentialsStore
import com.ultraviolince.mykitchen.domain.model.AuthState
import com.ultraviolince.mykitchen.domain.model.Recipe
import com.ultraviolince.mykitchen.domain.model.RecipeOrder
import com.ultraviolince.mykitchen.domain.model.SessionExpiredException
import com.ultraviolince.mykitchen.domain.model.User
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import com.ultraviolince.mykitchen.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class RecipeRepositoryImpl(
    private val dao: RecipeDao,
    private val api: RecipeApiClient,
    private val credentials: CredentialsStore,
) : RecipeRepository {

    override fun getRecipes(order: RecipeOrder): Flow<List<Recipe>> {
        val flow = when (order) {
            is RecipeOrder.Title -> dao.getRecipesByTitle()
            is RecipeOrder.Date -> dao.getRecipesByDate()
        }
        return flow.map { recipes ->
            if (order.ascending) recipes else recipes.reversed()
        }
    }

    override suspend fun getRecipeById(id: String): Recipe? = dao.getById(id)

    override suspend fun insertRecipe(recipe: Recipe) {
        dao.insert(recipe)
    }

    override suspend fun deleteRecipe(id: String) {
        dao.softDelete(id)
    }

    override suspend fun syncRecipes(): Result<Unit> {
        val token = credentials.getToken()
            ?: return Result.failure(IllegalStateException("Not logged in"))
        val serverUrl = credentials.getServerUrl()
            ?: return Result.failure(IllegalStateException("No server URL"))

        val remoteResult = api.getRecipes(serverUrl, token)
        val remoteRecipes = remoteResult.getOrElse { error ->
            if (error is ResponseException && error.response.status == HttpStatusCode.Unauthorized) {
                return Result.failure(SessionExpiredException())
            }
            return Result.failure(error)
        }
        remoteRecipes.forEach { dto -> dao.insert(dto.toDomain()) }

        val unsyncedIds = dao.getUnsyncedDeletedIds()
        for (id in unsyncedIds) {
            val result = api.deleteRecipe(serverUrl, token, id)
            if (result.isSuccess) dao.markSynced(id)
        }
        dao.clearSyncedDeleted()
        return Result.success(Unit)
    }

    override suspend fun login(email: String, password: String, serverUrl: String): Result<Unit> {
        val result = api.login(serverUrl, email, password)
        result.getOrNull()?.let { credentials.saveCredentials(it.token, serverUrl) }
        return result.map { }
    }

    override suspend fun logout() {
        credentials.clearCredentials()
    }

    override fun getAuthState(): Flow<AuthState> =
        combine(credentials.observeToken(), credentials.observeServerUrl()) { token, serverUrl ->
            if (token != null && serverUrl != null) {
                AuthState.LoggedIn(User(email = "", serverUrl = serverUrl, token = token))
            } else {
                AuthState.LoggedOut
            }
        }
}
