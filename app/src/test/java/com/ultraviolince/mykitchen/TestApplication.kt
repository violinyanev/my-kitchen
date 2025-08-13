package com.ultraviolince.mykitchen

import android.app.Application
import com.ultraviolince.mykitchen.di.AppModule
import com.ultraviolince.mykitchen.di.TestModule
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
            androidContext(this@TestApplication)
            modules(
                AppModule().module,
                TestModule().module,
            )
        }
    }
}
