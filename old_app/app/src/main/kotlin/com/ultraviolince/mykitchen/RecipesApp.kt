package com.ultraviolince.mykitchen

import android.app.Application
import com.ultraviolince.mykitchen.di.AppModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module

class RecipesApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@RecipesApp)
            modules(
                AppModule().module,
            )
        }
    }
}
