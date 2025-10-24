package com.ultraviolince.mykitchen.recipes.data.datasource.datastore

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SafeDataStoreTest {

    @Test
    fun `UserPreferences with empty values creates valid object`() {
        // Test that empty UserPreferences (fallback scenario) works correctly
        val preferences = UserPreferences()

        assertThat(preferences.server).isNull()
        assertThat(preferences.token).isNull()
    }

    @Test
    fun `UserPreferences with valid values creates correct object`() {
        // Test that normal UserPreferences creation works
        val server = "https://test-server.com"
        val token = "test-token-123"
        val preferences = UserPreferences(server, token)

        assertThat(preferences.server).isEqualTo(server)
        assertThat(preferences.token).isEqualTo(token)
    }

    @Test
    fun `UserPreferences equality works with null values`() {
        // Test that UserPreferences equality works correctly with null values
        val prefs1 = UserPreferences()
        val prefs2 = UserPreferences(null, null)
        val prefs3 = UserPreferences("server", null)

        assertThat(prefs1).isEqualTo(prefs2)
        assertThat(prefs1).isNotEqualTo(prefs3)
    }

    @Test
    fun `UserPreferences copy works correctly`() {
        // Test that UserPreferences copy functionality works
        val original = UserPreferences("original-server", "original-token")
        val copied = original.copy(server = "new-server")

        assertThat(copied.server).isEqualTo("new-server")
        assertThat(copied.token).isEqualTo("original-token")
        assertThat(original.server).isEqualTo("original-server") // Original unchanged
    }
}