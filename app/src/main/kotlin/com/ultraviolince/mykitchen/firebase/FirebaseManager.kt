package com.ultraviolince.mykitchen.firebase

import android.content.Context
import com.google.firebase.FirebaseApp
import com.ultraviolince.mykitchen.firebase.analytics.FirebaseAnalyticsService
import com.ultraviolince.mykitchen.firebase.crashlytics.FirebaseCrashlyticsService
import org.koin.core.annotation.Single

/**
 * Firebase Manager to coordinate Firebase services initialization and usage.
 * This class provides a central point for Firebase functionality in the app.
 */
@Single
class FirebaseManager(
    private val context: Context,
    private val analyticsService: FirebaseAnalyticsService,
    private val crashlyticsService: FirebaseCrashlyticsService
) {

    /**
     * Initialize Firebase if not already initialized
     */
    fun initialize() {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }

        // Log app startup
        analyticsService.logAppStartup()

        // Set initial custom keys for crashlytics
        crashlyticsService.setCustomKey("app_version", getAppVersion())
    }

    /**
     * Get the Analytics service
     */
    fun getAnalytics(): FirebaseAnalyticsService = analyticsService

    /**
     * Get the Crashlytics service
     */
    fun getCrashlytics(): FirebaseCrashlyticsService = crashlyticsService

    /**
     * Handle user login for Firebase services
     */
    fun onUserLogin(userId: String? = null, method: String = "backend") {
        analyticsService.logUserLogin(method)
        userId?.let {
            crashlyticsService.setUserId(it)
            analyticsService.setUserProperty("user_type", "authenticated")
        }
    }

    /**
     * Handle user logout for Firebase services
     */
    fun onUserLogout() {
        analyticsService.logUserLogout()
        crashlyticsService.setUserId("")
        analyticsService.setUserProperty("user_type", "anonymous")
    }

    /**
     * Handle global error logging
     */
    fun logError(error: Throwable, context: String? = null) {
        context?.let { crashlyticsService.log("Error context: $it") }
        crashlyticsService.recordException(error)
    }

    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }
}
