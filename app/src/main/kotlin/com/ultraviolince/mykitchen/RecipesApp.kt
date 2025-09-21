package com.ultraviolince.mykitchen

import android.app.Application
import com.ultraviolince.mykitchen.di.AppModule
import com.ultraviolince.mykitchen.recipes.presentation.util.PerfTracer
import com.ultraviolince.mykitchen.recipes.data.analytics.AnalyticsManager
import com.ultraviolince.mykitchen.recipes.data.analytics.AnalyticsConfig
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module

class RecipesApp : Application() {
    
    private val analyticsManager: AnalyticsManager by inject()
    
    override fun onCreate() {
        super.onCreate()

        PerfTracer.beginAsyncSection("AppStartup")

        startKoin {
            androidContext(this@RecipesApp)
            modules(
                AppModule().module,
            )
        }
        
        // Initialize analytics after Koin setup
        initializeAnalytics()
    }
    
    private fun initializeAnalytics() {
        try {
            // Only initialize if analytics is enabled
            if (AnalyticsConfig.isEnabled) {
                // In a real implementation, these would come from BuildConfig or environment
                val serverUrl = AnalyticsConfig.DEFAULT_SERVER_URL
                val appKey = AnalyticsConfig.DEFAULT_APP_KEY
                
                analyticsManager.initialize(serverUrl, appKey)
            }
        } catch (e: Exception) {
            // Ensure analytics failures don't crash the app
            e.printStackTrace()
        }
    }
}
