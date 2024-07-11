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

    private val _stage = mutableStateOf<LoginScreenStage>(LoginScreenStage.Loading)
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
            .onEach {defaultUser ->
                if(defaultUser != null) {
                    if (!defaultUser.token.isNullOrEmpty()) {
                        // TODO check that token is valid
                        recipesUseCases.login(defaultUser, null)
                        _eventFlow.emit(
                            UiEvent.LoginSuccess
                        )
                    }
                    else {
                        _stage.value = LoginScreenStage.EnterPassword(defaultUser)
                    }
                }
                else {
                    _eventFlow.emit(
                        UiEvent.CreateUser(null)
                    )
                }
            }
            .launchIn(viewModelScope)
    }


    fun onEvent(event: LoginEvent) {
        when (event) {
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
                    val pw = _stage.value as? LoginScreenStage.EnterPassword
                    if (pw != null)
                    {
                        try {
                            // TODO fix
                            recipesUseCases.login(
                                user = pw.user,
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
                                        //_stage.value = LoginScreenStage.EnterPassword(user)
                                    }
                                    is LoginState.LoginFailure -> {
                                        _stage.value = LoginScreenStage.EnterPassword(pw.user)
                                        _eventFlow.emit(
                                            UiEvent.ShowSnackbar(it.errorMessage)
                                        )
                                    }
                                    LoginState.LoginPending -> {
                                        _stage.value = LoginScreenStage.AwaitServerResponse(pw.user)
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
    }

    sealed class UiEvent {
        data class ShowSnackbar(@StringRes val message: Int) : UiEvent()
        data class CreateUser(val userId: Long?) : UiEvent()
        data object LoginSuccess : UiEvent()
    }
}
