package com.ultraviolince.mykitchen.recipes.di

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.ultraviolince.mykitchen.di.AppModule
import com.ultraviolince.mykitchen.di.TestModule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.koinApplication
import org.koin.plugin.module.dsl.modules
import org.koin.test.KoinTest
import org.koin.test.check.checkModules
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class KoinTest : KoinTest {

    @Suppress("DEPRECATION")
    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun checkAllModules() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        koinApplication {
            androidContext(app)
            modules(AppModule::class, TestModule::class)
        }.checkModules()
    }
}
