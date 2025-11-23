package com.ultraviolince.mykitchen.recipes.data.analytics

import android.content.Context
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class AnalyticsManagerTest {

    private lateinit var mockContext: Context
    private lateinit var analyticsManager: AnalyticsManager

    @Before
    fun setUp() {
        mockContext = mockk(relaxed = true)
        analyticsManager = AnalyticsManager(mockContext)
    }

    @Test
    fun `initial state is not ready`() {
        assertThat(analyticsManager.isReady()).isFalse()
    }

    @Test
    fun `initialize sets analytics as ready`() {
        analyticsManager.initialize("https://test.countly.com", "test-app-key")

        assertThat(analyticsManager.isReady()).isTrue()
    }

    @Test
    fun `initialize twice does not crash`() {
        analyticsManager.initialize("https://test.countly.com", "test-app-key")
        analyticsManager.initialize("https://test2.countly.com", "test-app-key-2")

        // Should still be ready and not crash
        assertThat(analyticsManager.isReady()).isTrue()
    }

    @Test
    fun `trackEvent does not crash when not initialized`() {
        // Should not crash even if not initialized
        analyticsManager.trackEvent("test_event")
    }

    @Test
    fun `trackEvent does not crash when initialized`() {
        analyticsManager.initialize("https://test.countly.com", "test-app-key")

        // Should not crash
        analyticsManager.trackEvent("test_event")
        analyticsManager.trackEvent("test_event_with_data", mapOf("key" to "value"))
    }

    @Test
    fun `trackRecipeEvent does not crash`() {
        analyticsManager.initialize("https://test.countly.com", "test-app-key")

        analyticsManager.trackRecipeEvent("create")
        analyticsManager.trackRecipeEvent("update", 123L)
    }

    @Test
    fun `trackAuthEvent does not crash`() {
        analyticsManager.initialize("https://test.countly.com", "test-app-key")

        analyticsManager.trackAuthEvent("login", true)
        analyticsManager.trackAuthEvent("login", false)
    }

    @Test
    fun `trackNavigation does not crash`() {
        analyticsManager.initialize("https://test.countly.com", "test-app-key")

        analyticsManager.trackNavigation("recipe_list")
    }

    @Test
    fun `recordException does not crash`() {
        analyticsManager.initialize("https://test.countly.com", "test-app-key")

        val testException = RuntimeException("Test exception")
        analyticsManager.recordException(testException)
        analyticsManager.recordException(testException, "Custom message")
    }

    @Test
    fun `recordException does not crash when not initialized`() {
        val testException = RuntimeException("Test exception")
        analyticsManager.recordException(testException)
    }
}
