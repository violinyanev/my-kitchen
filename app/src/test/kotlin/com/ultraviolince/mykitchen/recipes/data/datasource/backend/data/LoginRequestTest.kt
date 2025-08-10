package com.ultraviolince.mykitchen.recipes.data.datasource.backend.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LoginRequestTest {

    @Test
    fun `LoginRequest holds email and password correctly`() {
        val email = "test@example.com"
        val password = "password123"
        val loginRequest = LoginRequest(email, password)

        assertThat(loginRequest.email).isEqualTo(email)
        assertThat(loginRequest.password).isEqualTo(password)
    }

    @Test
    fun `LoginRequest with empty values`() {
        val loginRequest = LoginRequest("", "")

        assertThat(loginRequest.email).isEmpty()
        assertThat(loginRequest.password).isEmpty()
    }

    @Test
    fun `LoginRequest equality works correctly`() {
        val request1 = LoginRequest("user@test.com", "pass")
        val request2 = LoginRequest("user@test.com", "pass")
        val request3 = LoginRequest("other@test.com", "pass")

        assertThat(request1).isEqualTo(request2)
        assertThat(request1).isNotEqualTo(request3)
    }

    @Test
    fun `LoginRequest copy works correctly`() {
        val original = LoginRequest("user@test.com", "password")
        val copied = original.copy(password = "newPassword")

        assertThat(copied.email).isEqualTo(original.email)
        assertThat(copied.password).isEqualTo("newPassword")
        assertThat(copied).isNotEqualTo(original)
    }
}
