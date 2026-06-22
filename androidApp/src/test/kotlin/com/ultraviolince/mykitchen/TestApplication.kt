package com.ultraviolince.mykitchen

import android.app.Application
import android.content.Context
import com.ultraviolince.mykitchen.data.di.dataModule
import com.ultraviolince.mykitchen.data.di.platformDataModule
import com.ultraviolince.mykitchen.domain.di.domainModule
import com.ultraviolince.mykitchen.ui.di.uiModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

class TestApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initCmpAndroidContext(applicationContext)
        if (GlobalContext.getOrNull() == null) {
            startKoin {
                androidContext(this@TestApplication)
                modules(platformDataModule, dataModule, domainModule, uiModule)
            }
        }
    }
}

// CMP-6676: Robolectric doesn't reliably invoke AndroidContextProvider before tests run.
// org.jetbrains.compose.resources is not on the androidApp test compile classpath (non-KMP module),
// so we use reflection to set the Android context on DefaultAndroidResourceReader.
private fun initCmpAndroidContext(ctx: Context) {
    runCatching {
        val readerCls = Class.forName("org.jetbrains.compose.resources.DefaultAndroidResourceReader")
        val instance = readerCls.getDeclaredField("INSTANCE").also { it.isAccessible = true }.get(null)
        readerCls.declaredFields
            .firstOrNull { Context::class.java.isAssignableFrom(it.type) }
            ?.also { it.isAccessible = true; it.set(instance, ctx) }
    }
}
