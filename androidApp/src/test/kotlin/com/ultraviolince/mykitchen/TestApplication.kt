package com.ultraviolince.mykitchen

import android.content.pm.ProviderInfo
import org.jetbrains.compose.resources.ResourcesInitializer

class TestApplication : MyKitchenApp() {
    override fun onCreate() {
        // Robolectric doesn't invoke ContentProviders declared in the manifest
        // before the first composable render, so CMP's AndroidContextHolder is null.
        // Manually trigger ResourcesInitializer so compose-resources can find assets.
        ResourcesInitializer().attachInfo(
            this,
            ProviderInfo().apply {
                authority = "$packageName.ResourcesInitializer"
                this.packageName = this@TestApplication.packageName
                exported = false
                multiprocess = true
            }
        )
        super.onCreate()
    }
}
