package com.ultraviolince.mykitchen.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultraviolince.mykitchen.domain.usecase.LoginUseCase
import com.ultraviolince.mykitchen.ui.DefaultCredentials
import com.ultraviolince.mykitchen.ui.generated.resources.Res
import com.ultraviolince.mykitchen.ui.generated.resources.error_fields_required
import com.ultraviolince.mykitchen.ui.generated.resources.error_login_failed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

data class LoginState(
    val email: String = "",
    val password: String = "",
    val serverUrl: String = "http://localhost:5000",
    val isLoading: Boolean = false,
    val error: StringResource? = null,
    val isLoggedIn: Boolean = false,
)

class LoginViewModel(
    private val login: LoginUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun onEmailChange(email: String) {
        _state.update { it.copy(email = email, error = null) }
    }

    fun onPasswordChange(password: String) {
        _state.update { it.copy(password = password, error = null) }
    }

    fun onServerUrlChange(url: String) {
        _state.update { it.copy(serverUrl = url, error = null) }
    }

    fun applyDefaultCredentials(credentials: DefaultCredentials) {
        _state.update {
            it.copy(
                serverUrl = credentials.serverUrl ?: it.serverUrl,
                email = credentials.email ?: it.email,
                password = credentials.password ?: it.password,
            )
        }
    }

    fun login() {
        val state = _state.value
        if (state.email.isBlank() || state.password.isBlank() || state.serverUrl.isBlank()) {
            _state.update { it.copy(error = Res.string.error_fields_required) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = login.invoke(state.email.trim(), state.password, state.serverUrl.trim())
            if (result.isSuccess) {
                _state.update { it.copy(isLoading = false, isLoggedIn = true) }
            } else {
                _state.update { it.copy(isLoading = false, error = Res.string.error_login_failed) }
            }
        }
    }
}
