package com.ultraviolince.mykitchen.recipes.data.analytics

import android.content.Context
// import ly.count.android.sdk.Countly
// import ly.count.android.sdk.CountlyConfig
import org.koin.core.annotation.Single

/**
 * Analytics manager that wraps Countly SDK for tracking app usage and crashes.
 * Provides a minimal interface for essential feature tracking.
 */
@Single
class AnalyticsManager(private val context: Context) {

    private var isInitialized = false

    /**
     * Initialize Countly with the provided configuration.
     * Should be called once during app startup.
     */
    fun initialize(serverUrl: String, appKey: String) {
        if (isInitialized) return

        try {
            // TODO: Enable when Countly dependency is working
            /*
            val config = CountlyConfig(context, appKey, serverUrl)
                .setLoggingEnabled(false) // Disable logging for production
                .enableCrashReporting() // Enable crash reporting
                .setRequiresConsent(false) // For minimal integration, no consent required

            Countly.sharedInstance().init(config)
            */
            isInitialized = true
            
            // Track app start
            trackEvent("app_started")
        } catch (e: Exception) {
            // Fail silently to not crash the app if analytics fails
            e.printStackTrace()
        }
    }

    /**
     * Track a custom event with optional segmentation data.
     */
    fun trackEvent(eventName: String, segmentation: Map<String, Any>? = null) {
        if (!isInitialized) return

        try {
            // TODO: Enable when Countly dependency is working
            /*
            if (segmentation != null) {
                Countly.sharedInstance().recordEvent(eventName, segmentation)
            } else {
                Countly.sharedInstance().recordEvent(eventName)
            }
            */
            // For now, just log to console
            println("Analytics: $eventName with data: $segmentation")
        } catch (e: Exception) {
            // Fail silently
            e.printStackTrace()
        }
    }

    /**
     * Track recipe-related events (CRUD operations).
     */
    fun trackRecipeEvent(action: String, recipeId: Long? = null) {
        val segmentation = mutableMapOf<String, Any>("action" to action)
        recipeId?.let { segmentation["recipe_id"] = it }
        trackEvent("recipe_operation", segmentation)
    }

    /**
     * Track authentication events.
     */
    fun trackAuthEvent(action: String, success: Boolean = true) {
        val segmentation = mapOf(
            "action" to action,
            "success" to success
        )
        trackEvent("auth_operation", segmentation)
    }

    /**
     * Track navigation events.
     */
    fun trackNavigation(screenName: String) {
        val segmentation = mapOf("screen" to screenName)
        trackEvent("navigation", segmentation)
    }

    /**
     * Record a handled exception for crash analytics.
     */
    fun recordException(exception: Throwable, message: String? = null) {
        if (!isInitialized) return

        try {
            val crashDetails = mutableMapOf<String, Any>()
            message?.let { crashDetails["custom_message"] = it }
            crashDetails["exception_type"] = exception.javaClass.simpleName
            
            // TODO: Enable when Countly dependency is working
            // Countly.sharedInstance().crashes().recordHandledException(exception)
            println("Analytics: Exception recorded - ${exception.javaClass.simpleName}: ${exception.message}")
        } catch (e: Exception) {
            // Fail silently
            e.printStackTrace()
        }
    }

    /**
     * Check if analytics is properly initialized.
     */
    fun isReady(): Boolean = isInitialized
}