package com.ultraviolince.mykitchen

import android.app.Application
import android.os.StrictMode
import com.ultraviolince.mykitchen.di.AppModule
import com.ultraviolince.mykitchen.recipes.presentation.util.PerfTracer
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module

class RecipesApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize StrictMode early to catch violations throughout the app lifecycle
        initializeStrictMode()

        PerfTracer.beginAsyncSection("AppStartup")

        startKoin {
            androidContext(this@RecipesApp)
            modules(
                AppModule().module,
            )
        }
    }

    private fun initializeStrictMode() {
        // Check if running in test mode to avoid crashing during instrumented tests
        val isTestMode = isRunningInTestMode()

        if (BuildConfig.DEBUG) {
            // Enable comprehensive StrictMode for debug builds
            enableDebugStrictMode(isTestMode)
        } else {
            // Enable minimal StrictMode for release builds to catch critical issues
            enableReleaseStrictMode(isTestMode)
        }
    }

    private fun isRunningInTestMode(): Boolean {
        // Check if we're running under instrumentation or testing framework
        return try {
            Class.forName("androidx.test.espresso.Espresso") != null ||
                Class.forName("org.junit.runner.Runner") != null ||
                Class.forName("androidx.test.runner.AndroidJUnitRunner") != null ||
                System.getProperty("java.class.path")?.contains("test") == true
        } catch (e: ClassNotFoundException) {
            false
        }
    }

    private fun enableDebugStrictMode(isTestMode: Boolean) {
        // Thread policy - detect main thread violations
        val threadPolicyBuilder = StrictMode.ThreadPolicy.Builder()
            .detectDiskReads()
            .detectDiskWrites()
            .detectNetwork()
            .detectCustomSlowCalls()
            .detectResourceMismatches()

        if (isTestMode) {
            // In test mode, use logging instead of death penalty to avoid test crashes
            threadPolicyBuilder
                .penaltyLog()
                .penaltyFlashScreen() // Visual indication for developers
        } else {
            // In normal debug mode, use death penalty for strict enforcement
            threadPolicyBuilder
                .penaltyDeath()
                .penaltyFlashScreen() // Visual indication for developers
        }

        StrictMode.setThreadPolicy(threadPolicyBuilder.build())

        // VM policy - detect memory leaks and resource violations
        val vmPolicyBuilder = StrictMode.VmPolicy.Builder()
            .detectActivityLeaks()
            .detectLeakedSqlLiteObjects()
            .detectLeakedClosableObjects()
            .detectLeakedRegistrationObjects()
            .detectFileUriExposure()
            .detectCleartextNetwork()
            .detectContentUriWithoutPermission()
            .detectUntaggedSockets()

        if (isTestMode) {
            // In test mode, use logging instead of death penalty
            vmPolicyBuilder.penaltyLog()
        } else {
            // In normal debug mode, use death penalty for strict enforcement
            vmPolicyBuilder.penaltyDeath()
        }

        StrictMode.setVmPolicy(vmPolicyBuilder.build())
    }

    private fun enableReleaseStrictMode(isTestMode: Boolean) {
        // Minimal StrictMode for release builds - only critical violations
        val threadPolicyBuilder = StrictMode.ThreadPolicy.Builder()
            .detectNetwork() // Network on main thread is always critical

        val vmPolicyBuilder = StrictMode.VmPolicy.Builder()
            .detectActivityLeaks() // Memory leaks are critical
            .detectLeakedSqlLiteObjects()
            .detectCleartextNetwork() // Security issue

        if (isTestMode) {
            // In test mode, use logging instead of death penalty even for release builds
            threadPolicyBuilder.penaltyLog()
            vmPolicyBuilder.penaltyLog()
        } else {
            // In normal release mode, use death penalty for critical violations
            threadPolicyBuilder.penaltyDeath()
            vmPolicyBuilder.penaltyDeath()
        }

        StrictMode.setThreadPolicy(threadPolicyBuilder.build())
        StrictMode.setVmPolicy(vmPolicyBuilder.build())
    }
}
