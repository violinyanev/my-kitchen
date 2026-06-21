package com.ultraviolince.mykitchen

import android.app.Application
import com.ultraviolince.mykitchen.data.di.dataModule
import com.ultraviolince.mykitchen.data.di.platformDataModule
import com.ultraviolince.mykitchen.domain.di.domainModule
import com.ultraviolince.mykitchen.ui.di.uiModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

class TestApplication : Application() {
    override fun onCreate() {
        // CMP's DefaultAndroidResourceReader requires AndroidContextHolder.context to be set
        // before stringResource() is called during composition. Robolectric doesn't invoke
        // ContentProviders before the first composable render, so we set it directly via
        // reflection before super.onCreate().
        initCmpContext()
        super.onCreate()
        if (GlobalContext.getOrNull() == null) {
            startKoin {
                androidContext(this@TestApplication)
                modules(platformDataModule, dataModule, domainModule, uiModule)
            }
        }
    }

    private fun initCmpContext() {
        try {
            val clazz = Class.forName("org.jetbrains.compose.resources.AndroidContextHolder")
            val instance = clazz.getField("INSTANCE").get(null)
            val setter = clazz.getMethod("setContext", android.content.Context::class.java)
            setter.invoke(instance, applicationContext)
        } catch (e: Exception) {
            android.util.Log.w("TestApplication", "CMP context init failed: ${e.message}")
        }
    }
}
