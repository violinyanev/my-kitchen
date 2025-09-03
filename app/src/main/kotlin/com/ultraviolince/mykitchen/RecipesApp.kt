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
        if (BuildConfig.DEBUG) {
            // Enable comprehensive StrictMode for debug builds
            enableDebugStrictMode()
        } else {
            // Enable minimal StrictMode for release builds to catch critical issues
            enableReleaseStrictMode()
        }
    }

    private fun enableDebugStrictMode() {
        // Thread policy - detect main thread violations
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .detectCustomSlowCalls()
                .detectResourceMismatches()
                .penaltyDeath()
                .penaltyFlashScreen() // Visual indication for developers
                .build()
        )

        // VM policy - detect memory leaks and resource violations
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectActivityLeaks()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .detectLeakedRegistrationObjects()
                .detectFileUriExposure()
                .detectCleartextNetwork()
                .detectContentUriWithoutPermission()
                .detectUntaggedSockets()
                .penaltyDeath()
                .build()
        )
    }

    private fun enableReleaseStrictMode() {
        // Minimal StrictMode for release builds - only critical violations
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectNetwork() // Network on main thread is always critical
                .penaltyDeath()
                .build()
        )

        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectActivityLeaks() // Memory leaks are critical
                .detectLeakedSqlLiteObjects()
                .detectCleartextNetwork() // Security issue
                .penaltyDeath()
                .build()
        )
    }
}
