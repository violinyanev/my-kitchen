package com.ultraviolince.mykitchen.recipes.presentation.login

import androidx.compose.ui.focus.FocusState

    data class ChangeServerFocus(val focusState: FocusState) : CreateUserEvent()
    data class EnteredUsername(val value: String) : CreateUserEvent()
    data class ChangeUsernameFocus(val focusState: FocusState) : CreateUserEvent()
    data class EnteredEmail(val value: String) : CreateUserEvent()
    data class ChangeEmailFocus(val focusState: FocusState) : CreateUserEvent()
    data object Login : CreateUserEvent()
}
