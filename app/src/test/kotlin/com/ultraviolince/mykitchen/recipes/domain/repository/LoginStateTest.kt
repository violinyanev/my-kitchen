package com.ultraviolince.mykitchen.recipes.domain.repository

import com.ultraviolince.mykitchen.recipes.data.datasource.backend.util.NetworkError
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LoginStateTest {

    @Test
    fun `LoginEmpty is singleton object`() {
        val state1 = LoginState.LoginEmpty
        val state2 = LoginState.LoginEmpty

        assertThat(state1).isSameInstanceAs(state2)
    }

    @Test
    fun `LoginPending is singleton object`() {
        val state1 = LoginState.LoginPending
        val state2 = LoginState.LoginPending

        assertThat(state1).isSameInstanceAs(state2)
    }

    @Test
    fun `LoginSuccess is singleton object`() {
        val state1 = LoginState.LoginSuccess
        val state2 = LoginState.LoginSuccess

        assertThat(state1).isSameInstanceAs(state2)
    }

    @Test
    fun `LoginFailure holds error correctly`() {
        val error = NetworkError.UNAUTHORIZED
        val state = LoginState.LoginFailure(error)

        assertThat(state.error).isEqualTo(error)
    }

    @Test
    fun `LoginFailure with different errors are not equal`() {
        val state1 = LoginState.LoginFailure(NetworkError.UNAUTHORIZED)
        val state2 = LoginState.LoginFailure(NetworkError.SERVER_ERROR)

        assertThat(state1).isNotEqualTo(state2)
    }

    @Test
    fun `LoginFailure with same error are equal`() {
        val state1 = LoginState.LoginFailure(NetworkError.NO_INTERNET)
        val state2 = LoginState.LoginFailure(NetworkError.NO_INTERNET)

        assertThat(state1).isEqualTo(state2)
    }

    @Test
    fun `all LoginState types are different`() {
        val empty = LoginState.LoginEmpty
        val pending = LoginState.LoginPending
        val success = LoginState.LoginSuccess
        val failure = LoginState.LoginFailure(NetworkError.UNKNOWN)

        assertThat(empty).isNotEqualTo(pending)
        assertThat(empty).isNotEqualTo(success)
        assertThat(empty).isNotEqualTo(failure)
        assertThat(pending).isNotEqualTo(success)
        assertThat(pending).isNotEqualTo(failure)
        assertThat(success).isNotEqualTo(failure)
    }
}
