package com.ultraviolince.mykitchen.recipes.presentation.login

import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
import com.ultraviolince.mykitchen.recipes.domain.usecase.Recipes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

/**
 * Shared login logic that can be used by both Android and Web platforms
 * This replaces the Android-specific LoginViewModel with a platform-agnostic implementation
 */
class SharedLoginManager(private val recipes: Recipes) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val _server = MutableStateFlow("http://localhost:5000")
    val server: StateFlow<String> = _server.asStateFlow()
    
    private val _username = MutableStateFlow("test@example.com")
    val username: StateFlow<String> = _username.asStateFlow()
    
    private val _password = MutableStateFlow("password")
    val password: StateFlow<String> = _password.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Combined state for easy consumption by UI
    val loginUiState: StateFlow<LoginUiState> = combine(
        server, username, password, isLoading, recipes.getSyncState()
    ) { server: String, username: String, password: String, isLoading: Boolean, syncState ->
        LoginUiState(
            server = server,
            username = username,
            password = password,
            isLoading = isLoading,
            isLoggedIn = syncState is LoginState.LoginSuccess,
            loginError = if (syncState is LoginState.LoginFailure) syncState.error.toString() else null
        )
    }.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LoginUiState(
            server = "http://localhost:5000",
            username = "test@example.com",
            password = "password",
            isLoading = false,
            isLoggedIn = false
        )
    )
    
    fun updateServer(value: String) {
        _server.value = value
    }
    
    fun updateUsername(value: String) {
        _username.value = value
    }
    
    fun updatePassword(value: String) {
        _password.value = value
    }
    
    fun login() {
        if (_isLoading.value) return
        
        val currentServer = _server.value.trim()
        val currentUsername = _username.value.trim()
        val currentPassword = _password.value.trim()
        
        if (currentServer.isEmpty() || currentUsername.isEmpty() || currentPassword.isEmpty()) {
            return
        }
        
        _isLoading.value = true
        scope.launch {
            try {
                recipes.login(currentServer, currentUsername, currentPassword)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun logout() {
        scope.launch {
            recipes.logout()
        }
    }
}

data class LoginUiState(
    val server: String,
    val username: String, 
    val password: String,
    val isLoading: Boolean,
    val isLoggedIn: Boolean,
    val loginError: String? = null
)