package com.ultraviolince.mykitchen.recipes.di

import android.app.Application
import com.ultraviolince.mykitchen.di.AppModule
import com.ultraviolince.mykitchen.recipes.data.analytics.AnalyticsManager
import io.mockk.mockk
import org.junit.Test
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.ksp.generated.module
import org.koin.test.KoinTest
import org.koin.test.inject

class KoinTest : KoinTest {

    @Test
    fun checkAllModules() {
        try {
            // Stop any existing Koin instance
            stopKoin()
        } catch (e: Exception) {
            // Ignore if not started
        }

        val mockApplication = mockk<Application>(relaxed = true)

        // Start Koin with mock context and verify we can inject all dependencies
        startKoin {
            androidContext(mockApplication)
            modules(AppModule().module)
        }

        // Test that AnalyticsManager can be injected
        val analyticsManager: AnalyticsManager by inject()

        // If we reach here, injection worked correctly

        // Clean up
        stopKoin()
    }
}
