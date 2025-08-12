package com.ultraviolince.mykitchen

import android.app.Application
import com.ultraviolince.mykitchen.recipes.presentation.util.PerfTracer
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RecipesApp : Application() {
    override fun onCreate() {
        super.onCreate()

        PerfTracer.beginAsyncSection("AppStartup")
    }
}
