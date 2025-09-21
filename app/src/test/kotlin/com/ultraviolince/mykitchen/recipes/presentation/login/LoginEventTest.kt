package com.ultraviolince.mykitchen.recipes.presentation.login

import androidx.compose.ui.focus.FocusState
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import org.junit.Test

class LoginEventTest {

    @Test
    fun should_createEnteredServerEvent_when_valueProvided() {
        // Given
        val server = "https://api.example.com"

        // When
        val event = LoginEvent.EnteredServer(server)

        // Then
        assertThat(event).isInstanceOf(LoginEvent.EnteredServer::class.java)
        assertThat(event.value).isEqualTo(server)
    }

    @Test
    fun should_createChangeServerFocusEvent_when_focusStateProvided() {
        // Given
        val focusState = mockk<FocusState>()

        // When
        val event = LoginEvent.ChangeServerFocus(focusState)

        // Then
        assertThat(event).isInstanceOf(LoginEvent.ChangeServerFocus::class.java)
        assertThat(event.focusState).isEqualTo(focusState)
    }

    @Test
    fun should_createEnteredUsernameEvent_when_valueProvided() {
        // Given
        val username = "user@example.com"

        // When
        val event = LoginEvent.EnteredUsername(username)

        // Then
        assertThat(event).isInstanceOf(LoginEvent.EnteredUsername::class.java)
        assertThat(event.value).isEqualTo(username)
    }

    @Test
    fun should_createChangeUsernameFocusEvent_when_focusStateProvided() {
        // Given
        val focusState = mockk<FocusState>()

        // When
        val event = LoginEvent.ChangeUsernameFocus(focusState)

        // Then
        assertThat(event).isInstanceOf(LoginEvent.ChangeUsernameFocus::class.java)
        assertThat(event.focusState).isEqualTo(focusState)
    }

    @Test
    fun should_createEnteredPasswordEvent_when_valueProvided() {
        // Given
        val password = "secretpassword123"

        // When
        val event = LoginEvent.EnteredPassword(password)

        // Then
        assertThat(event).isInstanceOf(LoginEvent.EnteredPassword::class.java)
        assertThat(event.value).isEqualTo(password)
    }

    @Test
    fun should_createChangePasswordFocusEvent_when_focusStateProvided() {
        // Given
        val focusState = mockk<FocusState>()

        // When
        val event = LoginEvent.ChangePasswordFocus(focusState)

        // Then
        assertThat(event).isInstanceOf(LoginEvent.ChangePasswordFocus::class.java)
        assertThat(event.focusState).isEqualTo(focusState)
    }

    @Test
    fun should_createLoginEvent_when_accessed() {
        // When
        val event = LoginEvent.Login

        // Then
        assertThat(event).isInstanceOf(LoginEvent.Login::class.java)
        assertThat(event).isEqualTo(LoginEvent.Login)
    }

    @Test
    fun should_createLogoutEvent_when_accessed() {
        // When
        val event = LoginEvent.Logout

        // Then
        assertThat(event).isInstanceOf(LoginEvent.Logout::class.java)
        assertThat(event).isEqualTo(LoginEvent.Logout)
    }

    @Test
    fun should_haveDifferentEventTypes_when_comparing() {
        // Given
        val serverEvent = LoginEvent.EnteredServer("server")
        val usernameEvent = LoginEvent.EnteredUsername("username")
        val passwordEvent = LoginEvent.EnteredPassword("password")
        val loginEvent = LoginEvent.Login
        val logoutEvent = LoginEvent.Logout

        // Then
        assertThat(serverEvent).isNotEqualTo(usernameEvent)
        assertThat(serverEvent).isNotEqualTo(passwordEvent)
        assertThat(serverEvent).isNotEqualTo(loginEvent)
        assertThat(serverEvent).isNotEqualTo(logoutEvent)
        assertThat(loginEvent).isNotEqualTo(logoutEvent)
    }

    @Test
    fun should_equalSameEvents_when_sameDataProvided() {
        // Given
        val server1 = LoginEvent.EnteredServer("same.server.com")
        val server2 = LoginEvent.EnteredServer("same.server.com")
        val server3 = LoginEvent.EnteredServer("different.server.com")

        // Then
        assertThat(server1).isEqualTo(server2)
        assertThat(server1).isNotEqualTo(server3)
    }

    @Test
    fun should_equalSingletonEvents_when_accessed() {
        // Given/When
        val login1 = LoginEvent.Login
        val login2 = LoginEvent.Login
        val logout1 = LoginEvent.Logout
        val logout2 = LoginEvent.Logout

        // Then
        assertThat(login1).isEqualTo(login2)
        assertThat(logout1).isEqualTo(logout2)
        assertThat(login1).isNotEqualTo(logout1)
    }

    @Test
    fun should_handleEmptyStrings_when_enteredValuesEmpty() {
        // Given
        val emptyServer = LoginEvent.EnteredServer("")
        val emptyUsername = LoginEvent.EnteredUsername("")
        val emptyPassword = LoginEvent.EnteredPassword("")

        // Then
        assertThat(emptyServer.value).isEqualTo("")
        assertThat(emptyUsername.value).isEqualTo("")
        assertThat(emptyPassword.value).isEqualTo("")
    }
}