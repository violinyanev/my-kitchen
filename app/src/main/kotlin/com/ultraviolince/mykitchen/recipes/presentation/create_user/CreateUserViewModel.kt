package com.ultraviolince.mykitchen.recipes.presentation.create_user

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
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
class CreateUserViewModel @Inject constructor(
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

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

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
            is CreateUserEvent.Finish -> {
                viewModelScope.launch {
                    try {
                        // TODO fix
                        val user = User(serverUri = server.value.text, email = email.value.text, isDefault = true, name = username.value.text)
                        recipesUseCases.createUser(
                            user = user
                        )
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
        data object UserCreated : UiEvent()
    }
}
