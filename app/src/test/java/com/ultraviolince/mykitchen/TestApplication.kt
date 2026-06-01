package com.ultraviolince.mykitchen

import android.app.Application
import com.ultraviolince.mykitchen.di.AppModule
import com.ultraviolince.mykitchen.di.TestModule
import com.ultraviolince.mykitchen.di.TestOverridesModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module

class TestApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Stop any existing Koin instance before starting a new one
        try {
            GlobalContext.stopKoin()
        } catch (e: Exception) {
            // Ignore if Koin is not started
        }

        startKoin {
            allowOverride(true)
            androidContext(this@TestApplication)
            modules(
                AppModule().module,
                TestModule().module,
                TestOverridesModule().module,
            )
        }
    }
}
