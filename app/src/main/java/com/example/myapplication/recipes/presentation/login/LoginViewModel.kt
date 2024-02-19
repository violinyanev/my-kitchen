package com.example.myapplication.recipes.presentation.login

import androidx.annotation.StringRes
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.R
import com.example.myapplication.recipes.domain.model.LoginException
import com.example.myapplication.recipes.domain.repository.LoginState
import com.example.myapplication.recipes.domain.usecase.Recipes
import com.example.myapplication.recipes.presentation.editrecipe.RecipeTextFieldState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val recipesUseCases: Recipes
) : ViewModel() {
    private val _server = mutableStateOf(
        RecipeTextFieldState(
            text = "https://ultraviolince.com:8019", // TODO remove default
            hintStringId = R.string.server_hint
        )
    )
    val server: State<RecipeTextFieldState> = _server
    private val _username = mutableStateOf(
        RecipeTextFieldState(
            hintStringId = R.string.username_hint,
            text = "test@user.com"
        )
    )
    val username: State<RecipeTextFieldState> = _username
    private val _buttonLoading = mutableStateOf(false)
    val buttonLoading: State<Boolean> = _buttonLoading

    private val _password = mutableStateOf(
        RecipeTextFieldState(
            hintStringId = R.string.password_hint,
            text = "TestPassword" // TODO remove default
        )
    )
    val password: State<RecipeTextFieldState> = _password

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EnteredServer -> {
                _server.value = server.value.copy(text = event.value)
            }
            is LoginEvent.ChangeServerFocus -> {
                _server.value = server.value.copy(
                    isHintVisible = !event.focusState.isFocused && server.value.text.isBlank()
                )
            }
            is LoginEvent.EnteredUsername -> {
                _username.value = username.value.copy(text = event.value)
            }
            is LoginEvent.ChangeUsernameFocus -> {
                _username.value = username.value.copy(
                    isHintVisible = !event.focusState.isFocused && username.value.text.isBlank()
                )
            }
            is LoginEvent.EnteredPassword -> {
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
                        recipesUseCases.getSyncState().collect() {
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
                                        UiEvent.ShowSnackbar(it.errorMessage)
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
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(@StringRes val message: Int) : UiEvent()
        data object LoginSuccess : UiEvent()
    }
}
