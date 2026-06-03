package com.ultraviolince.mykitchen

import android.app.Application
import com.ultraviolince.mykitchen.di.appModule
import com.ultraviolince.mykitchen.recipes.presentation.util.PerfTracer
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class RecipesApp : Application() {
    override fun onCreate() {
        super.onCreate()

        PerfTracer.beginAsyncSection("AppStartup")

        startKoin {
            androidContext(this@RecipesApp)
            modules(
                appModule,
            )
        }
    }
}
