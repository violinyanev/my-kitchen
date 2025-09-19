package com.ultraviolince.mykitchen.firebase.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import org.koin.core.annotation.Single

/**
 * Firebase Analytics service for tracking user events and application usage.
 * This service provides a simple interface for logging events to Firebase Analytics.
 */
@Single
class FirebaseAnalyticsService(private val context: Context) {

    private val firebaseAnalytics: FirebaseAnalytics by lazy {
        FirebaseAnalytics.getInstance(context)
    }

    /**
     * Log a custom event to Firebase Analytics
     * @param eventName The name of the event
     * @param parameters Optional parameters for the event
     */
    fun logEvent(eventName: String, parameters: Map<String, Any> = emptyMap()) {
        val bundle = Bundle().apply {
            parameters.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Double -> putDouble(key, value)
                    is Boolean -> putBoolean(key, value)
                    else -> putString(key, value.toString())
                }
            }
        }
        firebaseAnalytics.logEvent(eventName, bundle)
    }

    /**
     * Log recipe creation event
     */
    fun logRecipeCreated(recipeName: String) {
        logEvent("recipe_created", mapOf("recipe_name" to recipeName))
    }

    /**
     * Log recipe view event
     */
    fun logRecipeViewed(recipeName: String) {
        logEvent("recipe_viewed", mapOf("recipe_name" to recipeName))
    }

    /**
     * Log user login event
     */
    fun logUserLogin(method: String = "backend") {
        logEvent("login", mapOf("method" to method))
    }

    /**
     * Log user logout event
     */
    fun logUserLogout() {
        logEvent("logout")
    }

    /**
     * Log app startup event
     */
    fun logAppStartup() {
        logEvent("app_startup")
    }

    /**
     * Set user property
     */
    fun setUserProperty(name: String, value: String) {
        firebaseAnalytics.setUserProperty(name, value)
    }
}
