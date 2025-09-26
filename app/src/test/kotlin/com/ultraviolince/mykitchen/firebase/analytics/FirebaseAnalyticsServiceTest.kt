package com.ultraviolince.mykitchen.firebase.analytics

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class FirebaseAnalyticsServiceTest {

    private lateinit var analyticsService: FirebaseAnalyticsService
    private val mockContext = mockk<Context>(relaxed = true)
    private val mockFirebaseAnalytics = mockk<FirebaseAnalytics>(relaxed = true)

    @Before
    fun setUp() {
        mockkStatic(FirebaseAnalytics::class)
        every { FirebaseAnalytics.getInstance(any()) } returns mockFirebaseAnalytics
        analyticsService = FirebaseAnalyticsService(mockContext)
    }

    @Test
    fun `logEvent should log custom event with parameters`() {
        // Given
        val eventName = "test_event"
        val parameters = mapOf(
            "string_param" to "test",
            "int_param" to 42,
            "boolean_param" to true
        )

        // When
        analyticsService.logEvent(eventName, parameters)

        // Then
        verify { mockFirebaseAnalytics.logEvent(eventName, any()) }
    }

    @Test
    fun `logRecipeCreated should log recipe creation event`() {
        // Given
        val recipeName = "Test Recipe"

        // When
        analyticsService.logRecipeCreated(recipeName)

        // Then
        verify { mockFirebaseAnalytics.logEvent("recipe_created", any()) }
    }

    @Test
    fun `logRecipeViewed should log recipe view event`() {
        // Given
        val recipeName = "Test Recipe"

        // When
        analyticsService.logRecipeViewed(recipeName)

        // Then
        verify { mockFirebaseAnalytics.logEvent("recipe_viewed", any()) }
    }

    @Test
    fun `logUserLogin should log login event with method`() {
        // Given
        val method = "backend"

        // When
        analyticsService.logUserLogin(method)

        // Then
        verify { mockFirebaseAnalytics.logEvent("login", any()) }
    }

    @Test
    fun `logUserLogout should log logout event`() {
        // When
        analyticsService.logUserLogout()

        // Then
        verify { mockFirebaseAnalytics.logEvent("logout", any()) }
    }

    @Test
    fun `logAppStartup should log app startup event`() {
        // When
        analyticsService.logAppStartup()

        // Then
        verify { mockFirebaseAnalytics.logEvent("app_startup", any()) }
    }

    @Test
    fun `setUserProperty should set user property`() {
        // Given
        val name = "user_type"
        val value = "authenticated"

        // When
        analyticsService.setUserProperty(name, value)

        // Then
        verify { mockFirebaseAnalytics.setUserProperty(name, value) }
    }
}
