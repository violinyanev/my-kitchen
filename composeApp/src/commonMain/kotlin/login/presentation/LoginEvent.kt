package login.presentation

import androidx.compose.ui.focus.FocusState

sealed class LoginEvent {
    data class EnteredServer(val value: String) : LoginEvent()
    data class ChangeServerFocus(val focusState: FocusState) : LoginEvent()
    data class EnteredUsername(val value: String) : LoginEvent()
    data class ChangeUsernameFocus(val focusState: FocusState) : LoginEvent()
    data class EnteredPassword(val value: String) : LoginEvent()
    data class ChangePasswordFocus(val focusState: FocusState) : LoginEvent()
    data object Login : LoginEvent()
}
