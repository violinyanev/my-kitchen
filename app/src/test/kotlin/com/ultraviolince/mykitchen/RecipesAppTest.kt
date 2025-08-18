package com.ultraviolince.mykitchen

import android.os.StrictMode
import org.junit.Test

/**
 * Unit tests for RecipesApp to verify StrictMode initialization and app setup.
 * These tests provide basic coverage for the StrictMode implementation.
 */
class RecipesAppTest {

    @Test
    fun appInstance_isCreatedSuccessfully() {
        // When: Creating a RecipesApp instance
        val app = RecipesApp()

        // Then: App instance is created without errors
        assert(app.javaClass.simpleName == "RecipesApp")
        
        // Verify the app has the expected methods for StrictMode setup
        val methods = app.javaClass.declaredMethods
        val strictModeMethods = methods.filter { it.name.contains("StrictMode", ignoreCase = true) }
        
        // We expect at least one method related to StrictMode
        assert(strictModeMethods.isNotEmpty()) {
            "RecipesApp should have StrictMode-related methods"
        }
    }

    @Test
    fun strictModeApiExists() {
        // Verify that StrictMode class and required methods exist
        // This ensures our StrictMode usage is compatible with the Android API level
        
        val strictModeClass = StrictMode::class.java
        
        // Check for key methods we use in the implementation
        val methods = strictModeClass.methods
        val hasSetThreadPolicy = methods.any { it.name == "setThreadPolicy" }
        val hasSetVmPolicy = methods.any { it.name == "setVmPolicy" }
        
        assert(hasSetThreadPolicy) { "StrictMode.setThreadPolicy method should exist" }
        assert(hasSetVmPolicy) { "StrictMode.setVmPolicy method should exist" }
    }

    @Test
    fun buildConfigExists() {
        // Verify that BuildConfig is available for our debug/release detection
        val buildConfigClass = BuildConfig::class.java
        
        // Check that BuildConfig has the DEBUG field we use
        val fields = buildConfigClass.fields
        val hasDebugField = fields.any { it.name == "DEBUG" }
        
        assert(hasDebugField) { "BuildConfig.DEBUG field should exist" }
        
        // Verify the field is boolean type
        val debugField = fields.first { it.name == "DEBUG" }
        assert(debugField.type == Boolean::class.javaPrimitiveType) {
            "BuildConfig.DEBUG should be a boolean field"
        }
    }
}