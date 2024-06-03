package com.ultraviolince.mykitchen.recipes.presentation.create_user

import androidx.compose.ui.focus.FocusState

sealed class CreateUserEvent {
    data class EnteredServer(val value: String) : CreateUserEvent()
    data class ChangeServerFocus(val focusState: FocusState) : CreateUserEvent()
    data class EnteredUsername(val value: String) : CreateUserEvent()
    data class ChangeUsernameFocus(val focusState: FocusState) : CreateUserEvent()
    data class EnteredEmail(val value: String) : CreateUserEvent()
    data class ChangeEmailFocus(val focusState: FocusState) : CreateUserEvent()
    data object Finish : CreateUserEvent()
}
