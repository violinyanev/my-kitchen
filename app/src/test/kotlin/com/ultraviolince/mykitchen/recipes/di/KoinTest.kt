package com.ultraviolince.mykitchen.recipes.di

import com.ultraviolince.mykitchen.di.AppModule
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.ksp.generated.module
import org.koin.ksp.generated.com_ultraviolince_mykitchen_di_AppModule
import org.koin.test.KoinTest
import org.koin.test.verify.verify

class KoinTest : KoinTest {

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun checkAllModules() {
        AppModule().module.verify(extraTypes = listOf(androidx.lifecycle.SavedStateHandle::class))
    }
}
