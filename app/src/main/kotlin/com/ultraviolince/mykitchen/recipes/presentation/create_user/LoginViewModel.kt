package com.ultraviolince.mykitchen.recipes.presentation.login

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultraviolince.mykitchen.R
import com.ultraviolince.mykitchen.recipes.domain.model.LoginException
import com.ultraviolince.mykitchen.recipes.domain.model.User
import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
import com.ultraviolince.mykitchen.recipes.domain.usecase.Recipes
import com.ultraviolince.mykitchen.recipes.presentation.editrecipe.RecipeTextFieldState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
class LoginViewModel @Inject constructor(
    private val recipesUseCases: Recipes
) : ViewModel() {
    private val _server = mutableStateOf(
        RecipeTextFieldState(
            text = "",
            hintStringId = R.string.server_hint
        )
    )
    val server: State<RecipeTextFieldState> = _server
    private val _username = mutableStateOf(
        RecipeTextFieldState(
            hintStringId = R.string.username_hint,
            text = ""
        )
    )
    val username: State<RecipeTextFieldState> = _username

    private val _email = mutableStateOf(
        RecipeTextFieldState(
            hintStringId = R.string.email_hint,
            text = ""
        )
    )
    val email: State<RecipeTextFieldState> = _email

    private val _buttonLoading = mutableStateOf(false)
    val buttonLoading: State<Boolean> = _buttonLoading

    private val _stage = mutableStateOf(LoginScreenStage.LOADING)
    val stage: State<LoginScreenStage> = _stage

    private val _password = mutableStateOf(
        RecipeTextFieldState(
            hintStringId = R.string.password_hint,
            text = ""
        )
    )
    val password: State<RecipeTextFieldState> = _password

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var getDefaultUserData: Job? = null

    init {
        getUserDetails()
    }

    private fun getUserDetails() {
        getDefaultUserData?.cancel()
        getDefaultUserData = recipesUseCases.getDefaultUser()
            .onEach {user ->
                if(user != null) {
                    if (!user.token.isNullOrEmpty()) {
                        // TODO check that token is valid
                        recipesUseCases.login(user, null)
                        _eventFlow.emit(
                            UiEvent.LoginSuccess
                        )
                    }
                    else {
                        _server.value = server.value.copy(text = user.serverUri)
                        _username.value = username.value.copy(text = user.email)
                        _stage.value = LoginScreenStage.ENTER_PASSWORD
                    }
                }
                else {
                    _stage.value = LoginScreenStage.CREATE_USER
                }
            }
            .launchIn(viewModelScope)
    }


    fun onEvent(event: CreateUserEvent) {
        when (event) {
            is CreateUserEvent.EnteredServer -> {
                Log.i("Recipes", "User entered server name ${event.value}")
                _server.value = server.value.copy(text = event.value)
            }
            is CreateUserEvent.ChangeServerFocus -> {
                _server.value = server.value.copy(
                    isHintVisible = !event.focusState.isFocused && server.value.text.isBlank()
                )
            }
            is CreateUserEvent.EnteredUsername -> {
                Log.i("Recipes", "User entered user name ${event.value}")
                _username.value = username.value.copy(text = event.value)
            }
            is CreateUserEvent.ChangeUsernameFocus -> {
                _username.value = username.value.copy(
                    isHintVisible = !event.focusState.isFocused && username.value.text.isBlank()
                )
            }
            is CreateUserEvent.EnteredEmail -> {
                Log.i("Recipes", "User entered email ${event.value}")
                _email.value = email.value.copy(text = event.value)
            }
            is CreateUserEvent.ChangeEmailFocus -> {
                _email.value = email.value.copy(
                    isHintVisible = !event.focusState.isFocused && email.value.text.isBlank()
                )
            }
            is CreateUserEvent.EnteredPassword -> {
                Log.i("Recipes", "User entered a password with length ${event.value.length}")
                _password.value = password.value.copy(text = event.value)
            }
            is CreateUserEvent.ChangePasswordFocus -> {
                _password.value = password.value.copy(
                    isHintVisible = !event.focusState.isFocused && password.value.text.isBlank()
                )
            }
            is CreateUserEvent.Login -> {
                viewModelScope.launch {
                    try {
                        // TODO fix
                        val user = User(serverUri = server.value.text, email = username.value.text, isDefault = true, name = email.value.text)
                        recipesUseCases.login(
                            user = user,
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
