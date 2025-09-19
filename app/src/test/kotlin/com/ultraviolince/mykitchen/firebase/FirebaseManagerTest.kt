package com.ultraviolince.mykitchen.firebase

import android.content.Context
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseApp
import com.ultraviolince.mykitchen.firebase.analytics.FirebaseAnalyticsService
import com.ultraviolince.mykitchen.firebase.crashlytics.FirebaseCrashlyticsService
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class FirebaseManagerTest {

    private lateinit var firebaseManager: FirebaseManager
    private val mockContext = mockk<Context>(relaxed = true)
    private val mockAnalyticsService = mockk<FirebaseAnalyticsService>(relaxed = true)
    private val mockCrashlyticsService = mockk<FirebaseCrashlyticsService>(relaxed = true)

    @Before
    fun setUp() {
        mockkStatic(FirebaseApp::class)
        every { FirebaseApp.getApps(any()) } returns emptyList()
        every { FirebaseApp.initializeApp(any()) } returns mockk()
        every { mockContext.packageManager } returns mockk(relaxed = true)
        every { mockContext.packageName } returns "com.ultraviolince.mykitchen"

        firebaseManager = FirebaseManager(mockContext, mockAnalyticsService, mockCrashlyticsService)
    }

    @Test
    fun `initialize should initialize Firebase and log app startup`() {
        // When
        firebaseManager.initialize()

        // Then
        verify { FirebaseApp.initializeApp(mockContext) }
        verify { mockAnalyticsService.logAppStartup() }
        verify { mockCrashlyticsService.setCustomKey("app_version", any<String>()) }
    }

    @Test
    fun `onUserLogin should log analytics and set crashlytics user`() {
        // Given
        val userId = "test-user"
        val method = "backend"

        // When
        firebaseManager.onUserLogin(userId, method)

        // Then
        verify { mockAnalyticsService.logUserLogin(method) }
        verify { mockCrashlyticsService.setUserId(userId) }
        verify { mockAnalyticsService.setUserProperty("user_type", "authenticated") }
    }

    @Test
    fun `onUserLogout should log analytics and clear crashlytics user`() {
        // When
        firebaseManager.onUserLogout()

        // Then
        verify { mockAnalyticsService.logUserLogout() }
        verify { mockCrashlyticsService.setUserId("") }
        verify { mockAnalyticsService.setUserProperty("user_type", "anonymous") }
    }

    @Test
    fun `logError should record exception with context`() {
        // Given
        val error = RuntimeException("Test error")
        val context = "Test context"

        // When
        firebaseManager.logError(error, context)

        // Then
        verify { mockCrashlyticsService.log("Error context: $context") }
        verify { mockCrashlyticsService.recordException(error) }
    }

    @Test
    fun `getAnalytics should return analytics service`() {
        // When
        val result = firebaseManager.getAnalytics()

        // Then
        assertThat(result).isEqualTo(mockAnalyticsService)
    }

    @Test
    fun `getCrashlytics should return crashlytics service`() {
        // When
        val result = firebaseManager.getCrashlytics()

        // Then
        assertThat(result).isEqualTo(mockCrashlyticsService)
    }
}
