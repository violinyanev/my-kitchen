package com.ultraviolince.mykitchen.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserTest {
    @Test
    fun `AuthState LoggedIn holds user`() {
        val user = User("a@b.com", "http://localhost:5000", "tok")
        val state: AuthState = AuthState.LoggedIn(user)
        assertTrue(state is AuthState.LoggedIn)
        assertEquals("a@b.com", (state as AuthState.LoggedIn).user.email)
    }

    @Test
    fun `AuthState LoggedOut is singleton`() {
        val a: AuthState = AuthState.LoggedOut
        val b: AuthState = AuthState.LoggedOut
        assertEquals(a, b)
    }
}
