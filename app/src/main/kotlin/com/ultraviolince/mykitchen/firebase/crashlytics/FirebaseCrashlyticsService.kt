package com.ultraviolince.mykitchen.firebase.crashlytics

import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.koin.core.annotation.Single

/**
 * Firebase Crashlytics service for crash reporting and error tracking.
 * This service provides a simple interface for logging crashes and non-fatal errors.
 */
@Single
class FirebaseCrashlyticsService {

    private val crashlytics: FirebaseCrashlytics by lazy {
        FirebaseCrashlytics.getInstance()
    }

    /**
     * Log a non-fatal exception to Crashlytics
     * @param throwable The exception to log
     */
    fun recordException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    /**
     * Log a custom error message
     * @param message The error message to log
     */
    fun log(message: String) {
        crashlytics.log(message)
    }

    /**
     * Set a custom key-value pair for crash context
     * @param key The key for the custom data
     * @param value The value for the custom data
     */
    fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }

    /**
     * Set a custom key-value pair for crash context
     * @param key The key for the custom data
     * @param value The value for the custom data
     */
    fun setCustomKey(key: String, value: Boolean) {
        crashlytics.setCustomKey(key, value)
    }

    /**
     * Set a custom key-value pair for crash context
     * @param key The key for the custom data
     * @param value The value for the custom data
     */
    fun setCustomKey(key: String, value: Int) {
        crashlytics.setCustomKey(key, value)
    }

    /**
     * Set user identifier for crash reports
     * @param userId The user identifier
     */
    fun setUserId(userId: String) {
        crashlytics.setUserId(userId)
    }

    /**
     * Enable or disable crash collection
     * @param enabled Whether to enable crash collection
     */
    fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        crashlytics.setCrashlyticsCollectionEnabled(enabled)
    }

    /**
     * Log a recipe-related error
     */
    fun logRecipeError(error: String, recipeId: Long? = null) {
        setCustomKey("error_type", "recipe")
        recipeId?.let { setCustomKey("recipe_id", it.toInt()) }
        log("Recipe error: $error")
    }

    /**
     * Log a backend-related error
     */
    fun logBackendError(error: String, endpoint: String? = null) {
        setCustomKey("error_type", "backend")
        endpoint?.let { setCustomKey("endpoint", it) }
        log("Backend error: $error")
    }

    /**
     * Log a database-related error
     */
    fun logDatabaseError(error: String, operation: String? = null) {
        setCustomKey("error_type", "database")
        operation?.let { setCustomKey("db_operation", it) }
        log("Database error: $error")
    }
}
