package com.ultraviolince.mykitchen.recipes.data.repository

import com.ultraviolince.mykitchen.recipes.data.datasource.backend.JsRecipeServiceWrapper
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.util.NetworkError
import com.ultraviolince.mykitchen.recipes.data.datasource.datastore.LocalStorageDataStore
import com.ultraviolince.mykitchen.recipes.data.datasource.localstorage.LocalStorageRecipeDao
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
import com.ultraviolince.mykitchen.recipes.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class JsRecipeRepositoryImpl(
    private val localDao: LocalStorageRecipeDao,
    private val dataStore: LocalStorageDataStore,
    private val serviceWrapper: JsRecipeServiceWrapper
) : RecipeRepository {
    
    private val _loginState = MutableStateFlow<LoginState>(LoginState.LoginEmpty)
    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    
    init {
        // Load initial state
        _loginState.value = dataStore.getLoginState()
        _recipes.value = localDao.getAll()
    }
    
    override fun getRecipes(): Flow<List<Recipe>> {
        _recipes.value = localDao.getAll()
        return _recipes.asStateFlow()
    }
    
    override suspend fun getRecipeById(id: Long): Recipe? {
        return localDao.getById(id)
    }
    
    override suspend fun insertRecipe(recipe: Recipe): Long {
        val id = localDao.insert(recipe)
        _recipes.value = localDao.getAll()
        return id
    }
    
    override suspend fun deleteRecipe(recipe: Recipe) {
        localDao.delete(recipe)
        _recipes.value = localDao.getAll()
    }
    
    override suspend fun login(server: String, email: String, password: String) {
        try {
            _loginState.value = LoginState.LoginPending
            serviceWrapper.configure(server)
            
            val success = serviceWrapper.login(email, password)
            if (success) {
                dataStore.saveServer(server)
                dataStore.saveUsername(email)
                dataStore.saveLoginState(LoginState.LoginSuccess)
                _loginState.value = LoginState.LoginSuccess
            } else {
                _loginState.value = LoginState.LoginFailure(NetworkError.UNAUTHORIZED)
            }
        } catch (e: Exception) {
            _loginState.value = LoginState.LoginFailure(NetworkError.UNKNOWN)
        }
    }
    
    override suspend fun logout() {
        dataStore.clear()
        serviceWrapper.close()
        _loginState.value = LoginState.LoginEmpty
    }
    
    override fun getLoginState(): Flow<LoginState> {
        return _loginState.asStateFlow()
    }
}

