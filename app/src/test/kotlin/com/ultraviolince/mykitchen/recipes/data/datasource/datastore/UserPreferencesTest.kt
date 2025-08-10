package com.ultraviolince.mykitchen.recipes.data.datasource.datastore

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class UserPreferencesTest {

    @Test
    fun `UserPreferences default constructor creates empty preferences`() {
        val preferences = UserPreferences()

        assertThat(preferences.server).isNull()
        assertThat(preferences.token).isNull()
    }

    @Test
    fun `UserPreferences holds server and token correctly`() {
        val server = "http://example.com"
        val token = "abc123token"
        val preferences = UserPreferences(server, token)

        assertThat(preferences.server).isEqualTo(server)
        assertThat(preferences.token).isEqualTo(token)
    }

    @Test
    fun `UserPreferences with only server`() {
        val server = "http://test.com"
        val preferences = UserPreferences(server = server)

        assertThat(preferences.server).isEqualTo(server)
        assertThat(preferences.token).isNull()
    }

    @Test
    fun `UserPreferences with only token`() {
        val token = "token123"
        val preferences = UserPreferences(token = token)

        assertThat(preferences.server).isNull()
        assertThat(preferences.token).isEqualTo(token)
    }

    @Test
    fun `UserPreferences equality works correctly`() {
        val prefs1 = UserPreferences("server1", "token1")
        val prefs2 = UserPreferences("server1", "token1")
        val prefs3 = UserPreferences("server2", "token1")

        assertThat(prefs1).isEqualTo(prefs2)
        assertThat(prefs1).isNotEqualTo(prefs3)
    }

    @Test
    fun `UserPreferences copy works correctly`() {
        val original = UserPreferences("original-server", "original-token")
        val copied = original.copy(token = "new-token")

        assertThat(copied.server).isEqualTo(original.server)
        assertThat(copied.token).isEqualTo("new-token")
        assertThat(copied).isNotEqualTo(original)
    }

    @Test
    fun `UserPreferences hashCode works correctly`() {
        val prefs1 = UserPreferences("server", "token")
        val prefs2 = UserPreferences("server", "token")
        val prefs3 = UserPreferences("different", "token")

        assertThat(prefs1.hashCode()).isEqualTo(prefs2.hashCode())
        assertThat(prefs1.hashCode()).isNotEqualTo(prefs3.hashCode())
    }

    @Test
    fun `UserPreferencesSerializer has correct default value`() {
        val defaultValue = UserPreferencesSerializer.defaultValue

        assertThat(defaultValue).isEqualTo(UserPreferences())
        assertThat(defaultValue.server).isNull()
        assertThat(defaultValue.token).isNull()
    }
}
