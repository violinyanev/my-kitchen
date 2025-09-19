package com.ultraviolince.mykitchen.firebase.crashlytics

import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class FirebaseCrashlyticsServiceTest {

    private lateinit var crashlyticsService: FirebaseCrashlyticsService
    private val mockFirebaseCrashlytics = mockk<FirebaseCrashlytics>(relaxed = true)

    @Before
    fun setUp() {
        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns mockFirebaseCrashlytics
        crashlyticsService = FirebaseCrashlyticsService()
    }

    @Test
    fun `recordException should record exception`() {
        // Given
        val exception = RuntimeException("Test exception")

        // When
        crashlyticsService.recordException(exception)

        // Then
        verify { mockFirebaseCrashlytics.recordException(exception) }
    }

    @Test
    fun `log should log message`() {
        // Given
        val message = "Test message"

        // When
        crashlyticsService.log(message)

        // Then
        verify { mockFirebaseCrashlytics.log(message) }
    }

    @Test
    fun `setCustomKey should set string custom key`() {
        // Given
        val key = "test_key"
        val value = "test_value"

        // When
        crashlyticsService.setCustomKey(key, value)

        // Then
        verify { mockFirebaseCrashlytics.setCustomKey(key, value) }
    }

    @Test
    fun `setCustomKey should set boolean custom key`() {
        // Given
        val key = "test_key"
        val value = true

        // When
        crashlyticsService.setCustomKey(key, value)

        // Then
        verify { mockFirebaseCrashlytics.setCustomKey(key, value) }
    }

    @Test
    fun `setCustomKey should set int custom key`() {
        // Given
        val key = "test_key"
        val value = 42

        // When
        crashlyticsService.setCustomKey(key, value)

        // Then
        verify { mockFirebaseCrashlytics.setCustomKey(key, value) }
    }

    @Test
    fun `setUserId should set user id`() {
        // Given
        val userId = "test-user-123"

        // When
        crashlyticsService.setUserId(userId)

        // Then
        verify { mockFirebaseCrashlytics.setUserId(userId) }
    }

    @Test
    fun `setCrashlyticsCollectionEnabled should enable or disable collection`() {
        // When
        crashlyticsService.setCrashlyticsCollectionEnabled(true)

        // Then
        verify { mockFirebaseCrashlytics.setCrashlyticsCollectionEnabled(true) }
    }

    @Test
    fun `logRecipeError should log recipe error with context`() {
        // Given
        val error = "Recipe parsing failed"
        val recipeId = 123L

        // When
        crashlyticsService.logRecipeError(error, recipeId)

        // Then
        verify { mockFirebaseCrashlytics.setCustomKey("error_type", "recipe") }
        verify { mockFirebaseCrashlytics.setCustomKey("recipe_id", 123) }
        verify { mockFirebaseCrashlytics.log("Recipe error: $error") }
    }

    @Test
    fun `logBackendError should log backend error with endpoint`() {
        // Given
        val error = "Network timeout"
        val endpoint = "/api/recipes"

        // When
        crashlyticsService.logBackendError(error, endpoint)

        // Then
        verify { mockFirebaseCrashlytics.setCustomKey("error_type", "backend") }
        verify { mockFirebaseCrashlytics.setCustomKey("endpoint", endpoint) }
        verify { mockFirebaseCrashlytics.log("Backend error: $error") }
    }

    @Test
    fun `logDatabaseError should log database error with operation`() {
        // Given
        val error = "Query failed"
        val operation = "insert"

        // When
        crashlyticsService.logDatabaseError(error, operation)

        // Then
        verify { mockFirebaseCrashlytics.setCustomKey("error_type", "database") }
        verify { mockFirebaseCrashlytics.setCustomKey("db_operation", operation) }
        verify { mockFirebaseCrashlytics.log("Database error: $error") }
    }
}
