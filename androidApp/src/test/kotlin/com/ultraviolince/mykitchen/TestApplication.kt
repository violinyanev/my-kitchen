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
        super.onCreate()
        // CMP-6676: Robolectric doesn't reliably invoke AndroidContextProvider before tests run.
        // Set the context explicitly so stringResource() works during initial composition.
        setResourceReaderAndroidContext(this)
        if (GlobalContext.getOrNull() == null) {
            startKoin {
                androidContext(this@TestApplication)
                modules(platformDataModule, dataModule, domainModule, uiModule)
            }
        }
    }
}
