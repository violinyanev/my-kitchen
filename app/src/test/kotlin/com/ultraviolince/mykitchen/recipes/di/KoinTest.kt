package com.ultraviolince.mykitchen.recipes.di

import android.app.Application
import com.ultraviolince.mykitchen.di.AppModule
import com.ultraviolince.mykitchen.firebase.FirebaseManager
import com.ultraviolince.mykitchen.firebase.analytics.FirebaseAnalyticsService
import com.ultraviolince.mykitchen.firebase.crashlytics.FirebaseCrashlyticsService
import io.mockk.mockk
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.ksp.generated.module
import org.koin.test.KoinTest
import org.koin.test.verify.verify

class KoinTest : KoinTest {

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun checkAllModules() {
        val mockModule = module {
            single<Application> { mockk() }
            single<FirebaseAnalyticsService> { mockk() }
            single<FirebaseCrashlyticsService> { mockk() }
            single<FirebaseManager> { mockk() }
        }
        
        AppModule().module.verify(
            extraTypes = listOf(
                Application::class,
                FirebaseAnalyticsService::class,
                FirebaseCrashlyticsService::class,
                FirebaseManager::class
            )
        )
    }
}
