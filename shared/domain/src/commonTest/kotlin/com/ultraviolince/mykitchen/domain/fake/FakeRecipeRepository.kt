package com.ultraviolince.mykitchen.domain.fake

import com.ultraviolince.mykitchen.domain.model.AuthState
import com.ultraviolince.mykitchen.domain.model.Recipe
import com.ultraviolince.mykitchen.domain.model.RecipeOrder
import com.ultraviolince.mykitchen.domain.model.User
import com.ultraviolince.mykitchen.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeRecipeRepository : RecipeRepository {
    private val recipesFlow = MutableStateFlow<List<Recipe>>(emptyList())
    private val authFlow = MutableStateFlow<AuthState>(AuthState.LoggedOut)
    var loginResult: Result<Unit> = Result.success(Unit)
    var syncResult: Result<Unit> = Result.success(Unit)

    override fun getRecipes(order: RecipeOrder): Flow<List<Recipe>> =
        recipesFlow.map { recipes ->
            val active = recipes.filter { !it.deleted }
            when (order) {
                is RecipeOrder.Title -> if (order.ascending) active.sortedBy { it.title }
                                       else active.sortedByDescending { it.title }
                is RecipeOrder.Date  -> if (order.ascending) active.sortedBy { it.timestamp }
                                       else active.sortedByDescending { it.timestamp }
            }
        }

    override suspend fun getRecipeById(id: String): Recipe? =
        recipesFlow.value.find { it.id == id }

    override suspend fun insertRecipe(recipe: Recipe) {
        val current = recipesFlow.value.toMutableList()
        current.removeIf { it.id == recipe.id }
        current.add(recipe)
        recipesFlow.value = current
    }

    override suspend fun deleteRecipe(id: String) {
        recipesFlow.value = recipesFlow.value.map {
            if (it.id == id) it.copy(deleted = true) else it
        }
    }

    override suspend fun syncRecipes(): Result<Unit> = syncResult

    override suspend fun login(email: String, password: String, serverUrl: String): Result<Unit> {
        if (loginResult.isSuccess) {
            authFlow.value = AuthState.LoggedIn(User(email, serverUrl, "fake-token"))
        }
        return loginResult
    }

    override suspend fun logout() {
        authFlow.value = AuthState.LoggedOut
    }

    override fun getAuthState(): Flow<AuthState> = authFlow
}
