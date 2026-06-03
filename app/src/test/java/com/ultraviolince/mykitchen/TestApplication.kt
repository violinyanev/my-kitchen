package com.ultraviolince.mykitchen

import android.app.Application
import com.ultraviolince.mykitchen.di.appModule
import com.ultraviolince.mykitchen.di.testModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

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
                appModule,
                testModule,
            )
        }
    }
}
