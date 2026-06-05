package com.ultraviolince.mykitchen.data.repository

import com.ultraviolince.mykitchen.data.local.RecipeDao
import com.ultraviolince.mykitchen.data.local.toDomain
import com.ultraviolince.mykitchen.data.local.toEntity
import com.ultraviolince.mykitchen.data.remote.RecipeApiClient
import com.ultraviolince.mykitchen.data.remote.dto.toDto
import com.ultraviolince.mykitchen.data.remote.dto.toEntity
import com.ultraviolince.mykitchen.data.store.CredentialsStore
import com.ultraviolince.mykitchen.domain.model.AuthState
import com.ultraviolince.mykitchen.domain.model.Recipe
import com.ultraviolince.mykitchen.domain.model.RecipeOrder
import com.ultraviolince.mykitchen.domain.model.User
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
        return flow.map { entities ->
            val mapped = entities.map { it.toDomain() }
            if (order.ascending) mapped else mapped.reversed()
        }
    }

    override suspend fun getRecipeById(id: String): Recipe? =
        dao.getById(id)?.toDomain()

    override suspend fun insertRecipe(recipe: Recipe) {
        dao.insert(recipe.toEntity())
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
        if (remoteResult.isFailure) return Result.failure(remoteResult.exceptionOrNull()!!)

        val remoteRecipes = remoteResult.getOrNull()!!
        remoteRecipes.forEach { dto -> dao.insert(dto.toEntity()) }

        val unsynced = dao.getUnsynced()
        for (entity in unsynced) {
            val result = api.deleteRecipe(serverUrl, token, entity.id)
            if (result.isSuccess) dao.markSynced(entity.id)
        }
        dao.clearSyncedDeleted()
        return Result.success(Unit)
    }

    override suspend fun login(email: String, password: String, serverUrl: String): Result<Unit> {
        val result = api.login(serverUrl, email, password)
        if (result.isSuccess) {
            credentials.saveCredentials(result.getOrNull()!!.token, serverUrl)
        }
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
