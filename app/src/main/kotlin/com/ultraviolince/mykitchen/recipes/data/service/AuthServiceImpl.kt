package com.ultraviolince.mykitchen.recipes.data.service

import android.util.Log
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.RecipeService
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.data.LoginRequest
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.util.Result
import com.ultraviolince.mykitchen.recipes.data.datasource.datastore.SafeDataStore
import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
import com.ultraviolince.mykitchen.recipes.domain.service.AuthService
import com.ultraviolince.mykitchen.recipes.domain.service.NetworkService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

@Single
class AuthServiceImpl(
    private val dataStore: SafeDataStore,
    private val networkService: NetworkService
) : AuthService {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val _loginState = MutableStateFlow<LoginState>(LoginState.LoginEmpty)

    private var recipeService: RecipeService? = null

    init {
        scope.launch {
            val prefs = dataStore.preferences.first()

            Log.d("#auth", "Checking stored preferences")
            if (prefs.server != null && prefs.token != null) {
                Log.d("#auth", "Restoring data, server=${prefs.server}")
                recipeService = RecipeService(networkService.createHttpClient(prefs.server, prefs.token))

                // TODO: check if token still valid
                _loginState.emit(LoginState.LoginSuccess)
            }
        }
    }

    override suspend fun login(server: String, email: String, password: String) {
        _loginState.emit(LoginState.LoginPending)

        val tmpService = RecipeService(networkService.createHttpClient(server, null))

        val result = tmpService.login(LoginRequest(email, password))

        Log.i("#auth", "Login result: $result")
        when (result) {
            is Result.Error -> _loginState.emit(LoginState.LoginFailure(error = result.error))
            is Result.Success -> {
                recipeService = RecipeService(networkService.createHttpClient(server, result.data.data.token))
                dataStore.write(server = server, token = result.data.data.token)
                _loginState.emit(LoginState.LoginSuccess)
            }
        }
    }

    override suspend fun logout() {
        dataStore.write("", "")
        _loginState.emit(LoginState.LoginEmpty)
        recipeService = null
    }

    override fun getLoginState(): StateFlow<LoginState> {
        return _loginState
    }
}
