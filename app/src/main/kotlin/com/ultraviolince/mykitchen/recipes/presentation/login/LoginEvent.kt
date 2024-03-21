package com.ultraviolince.mykitchen.recipes.presentation.login

import androidx.compose.ui.focus.FocusState

sealed class LoginEvent {
    data class EnteredPassword(val value: String) : LoginEvent()
    data class ChangePasswordFocus(val focusState: FocusState) : LoginEvent()
    data object Login : LoginEvent()
}
