package com.ultraviolince.mykitchen.recipes.presentation.login

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultraviolince.mykitchen.BuildConfig
import com.ultraviolince.mykitchen.R
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.util.NetworkError
import com.ultraviolince.mykitchen.recipes.domain.model.LoginException
import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
import com.ultraviolince.mykitchen.recipes.domain.usecase.Recipes
import com.ultraviolince.mykitchen.recipes.presentation.editrecipe.RecipeTextFieldState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class LoginViewModel(
    private val recipesUseCases: Recipes
) : ViewModel() {

    val isLoggedIn = recipesUseCases.getSyncState().map { it == LoginState.LoginSuccess }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    private val _server = mutableStateOf(
        RecipeTextFieldState(
            text = BuildConfig.DEFAULT_SERVER,
            hintStringId = R.string.server_hint,

        )
    )
    val server: State<RecipeTextFieldState> = _server
    private val _username = mutableStateOf(
        RecipeTextFieldState(
            hintStringId = R.string.username_hint,
            text = BuildConfig.DEFAULT_USERNAME
        )
    )
    val username: State<RecipeTextFieldState> = _username
    private val _buttonLoading = mutableStateOf(false)
    val buttonLoading: State<Boolean> = _buttonLoading

    private val _password = mutableStateOf(
        RecipeTextFieldState(
            hintStringId = R.string.password_hint,
            text = BuildConfig.DEFAULT_PASSWORD
        )
    )
    val password: State<RecipeTextFieldState> = _password

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EnteredServer -> {
                Log.i("Recipes", "User entered server name ${event.value}")
                _server.value = server.value.copy(text = event.value)
            }
            is LoginEvent.ChangeServerFocus -> {
                _server.value = server.value.copy(
                    isHintVisible = !event.focusState.isFocused && server.value.text.isBlank()
                )
            }
            is LoginEvent.EnteredUsername -> {
                Log.i("Recipes", "User entered user name ${event.value}")
                _username.value = username.value.copy(text = event.value)
            }
            is LoginEvent.ChangeUsernameFocus -> {
                _username.value = username.value.copy(
                    isHintVisible = !event.focusState.isFocused && username.value.text.isBlank()
                )
            }
            is LoginEvent.EnteredPassword -> {
                Log.i("Recipes", "User entered a password with length ${event.value.length}")
                _password.value = password.value.copy(text = event.value)
            }
            is LoginEvent.ChangePasswordFocus -> {
                _password.value = password.value.copy(
                    isHintVisible = !event.focusState.isFocused && password.value.text.isBlank()
                )
            }
            is LoginEvent.Login -> {
                viewModelScope.launch {
                    try {
                        recipesUseCases.login(
                            server = server.value.text,
                            username = username.value.text,
                            password = password.value.text
                        )
                        recipesUseCases.getSyncState().collect {
                            when (it) {
                                is LoginState.LoginSuccess -> {
                                    _eventFlow.emit(
                                        UiEvent.LoginSuccess
                                    )
                                }
                                LoginState.LoginEmpty -> {
                                    _buttonLoading.value = false
                                }
                                is LoginState.LoginFailure -> {
                                    _buttonLoading.value = false
                                    _eventFlow.emit(
                                        UiEvent.ShowSnackbar(
                                            when (it.error) {
                                                // TODO fix all responses
                                                NetworkError.UNKNOWN -> R.string.unknown_error
                                                NetworkError.REQUEST_TIMEOUT -> R.string.malformed_server_uri
                                                NetworkError.UNAUTHORIZED -> R.string.unknown_error
                                                NetworkError.CONFLICT -> R.string.unknown_error
                                                NetworkError.TOO_MANY_REQUESTS -> R.string.unknown_error
                                                NetworkError.NO_INTERNET -> R.string.unknown_error
                                                NetworkError.PAYLOAD_TOO_LARGE -> R.string.unknown_error
                                                NetworkError.SERVER_ERROR -> R.string.malformed_server_uri
                                                NetworkError.SERIALIZATION -> R.string.unknown_error
                                            }
                                        )
                                    )
                                }
                                LoginState.LoginPending -> {
                                    _buttonLoading.value = true
                                }
                            }
                        }
                    } catch (e: LoginException) {
                        _eventFlow.emit(
                            UiEvent.ShowSnackbar(
                                message = e.errorMsg
                            )
                        )
                    }
                }
            }
            LoginEvent.Logout -> {
                viewModelScope.launch {
                    recipesUseCases.logout()
                }
            }
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(@StringRes val message: Int) : UiEvent()
        data object LoginSuccess : UiEvent()
    }
}
