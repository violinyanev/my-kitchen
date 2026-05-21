package com.ultraviolince.mykitchen

import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Unit tests for RecipesApp to verify StrictMode integration.
 * These tests verify the presence of required methods and Android compatibility.
 */
class RecipesAppTest {

    @Test
    fun `app has strictMode initialization methods`() {
        val app = RecipesApp()

        // Use reflection to verify the StrictMode methods exist
        val initializeStrictModeMethod = app.javaClass.getDeclaredMethod("initializeStrictMode")
        assertNotNull("initializeStrictMode method should exist", initializeStrictModeMethod)

        val isRunningInTestModeMethod = app.javaClass.getDeclaredMethod("isRunningInTestMode")
        assertNotNull("isRunningInTestMode method should exist", isRunningInTestModeMethod)

        val enableDebugStrictModeMethod = app.javaClass.getDeclaredMethod("enableDebugStrictMode", Boolean::class.java)
        assertNotNull("enableDebugStrictMode method should exist", enableDebugStrictModeMethod)

        val enableReleaseStrictModeMethod = app.javaClass.getDeclaredMethod("enableReleaseStrictMode", Boolean::class.java)
        assertNotNull("enableReleaseStrictMode method should exist", enableReleaseStrictModeMethod)
    }

    @Test
    fun `app buildConfig has debug field`() {
        // Verify BuildConfig.DEBUG field exists and is accessible
        val buildConfigClass = Class.forName("com.ultraviolince.mykitchen.BuildConfig")
        val debugField = buildConfigClass.getField("DEBUG")
        assertNotNull("BuildConfig.DEBUG field should exist", debugField)

        // The value can be either true or false depending on build variant
        val debugValue = debugField.get(null)
        assertNotNull("DEBUG field should have a value", debugValue)
    }

    @Test
    fun `app strictMode classes are accessible`() {
        // Verify StrictMode classes are available at runtime
        assertNotNull("StrictMode class should be available", Class.forName("android.os.StrictMode"))
        assertNotNull("ThreadPolicy.Builder should be available",
            Class.forName("android.os.StrictMode\$ThreadPolicy\$Builder"))
        assertNotNull("VmPolicy.Builder should be available",
            Class.forName("android.os.StrictMode\$VmPolicy\$Builder"))
    }
}
