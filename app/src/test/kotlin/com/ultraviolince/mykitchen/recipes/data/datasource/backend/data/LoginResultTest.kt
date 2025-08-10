package com.ultraviolince.mykitchen.recipes.data.datasource.backend.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LoginResultTest {

    @Test
    fun `LoginResultData holds username and token correctly`() {
        val username = "testuser"
        val token = "abc123token"
        val data = LoginResultData(username, token)

        assertThat(data.username).isEqualTo(username)
        assertThat(data.token).isEqualTo(token)
    }

    @Test
    fun `LoginResult holds data correctly`() {
        val data = LoginResultData("user", "token123")
        val result = LoginResult(data)

        assertThat(result.data).isEqualTo(data)
        assertThat(result.data.username).isEqualTo("user")
        assertThat(result.data.token).isEqualTo("token123")
    }

    @Test
    fun `LoginResultData equality works correctly`() {
        val data1 = LoginResultData("user1", "token1")
        val data2 = LoginResultData("user1", "token1")
        val data3 = LoginResultData("user2", "token1")

        assertThat(data1).isEqualTo(data2)
        assertThat(data1).isNotEqualTo(data3)
    }

    @Test
    fun `LoginResult equality works correctly`() {
        val data = LoginResultData("user", "token")
        val result1 = LoginResult(data)
        val result2 = LoginResult(data.copy())

        assertThat(result1).isEqualTo(result2)
    }

    @Test
    fun `LoginResultData copy works correctly`() {
        val original = LoginResultData("originalUser", "originalToken")
        val copied = original.copy(token = "newToken")

        assertThat(copied.username).isEqualTo(original.username)
        assertThat(copied.token).isEqualTo("newToken")
        assertThat(copied).isNotEqualTo(original)
    }

    @Test
    fun `LoginResult copy works correctly`() {
        val originalData = LoginResultData("user", "token")
        val original = LoginResult(originalData)
        val newData = originalData.copy(username = "newUser")
        val copied = original.copy(data = newData)

        assertThat(copied.data.username).isEqualTo("newUser")
        assertThat(copied.data.token).isEqualTo(original.data.token)
        assertThat(copied).isNotEqualTo(original)
    }
}
