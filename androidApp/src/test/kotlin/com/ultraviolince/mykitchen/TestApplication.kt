package com.ultraviolince.mykitchen

import android.app.Application
import com.ultraviolince.mykitchen.data.di.dataModule
import com.ultraviolince.mykitchen.data.di.platformDataModule
import com.ultraviolince.mykitchen.domain.di.domainModule
import com.ultraviolince.mykitchen.ui.di.uiModule
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.setResourceReaderAndroidContext
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

class TestApplication : Application() {
    @OptIn(ExperimentalResourceApi::class)
    override fun onCreate() {
        // Use the CMP public API to set the Android context before any composition.
        // AndroidContextProvider (the ContentProvider that normally does this) is not
        // reliably invoked by Robolectric before test composables run (CMP-6676).
        setResourceReaderAndroidContext(this)
        super.onCreate()
        if (GlobalContext.getOrNull() == null) {
            startKoin {
                androidContext(this@TestApplication)
                modules(platformDataModule, dataModule, domainModule, uiModule)
            }
        }
    }
}

