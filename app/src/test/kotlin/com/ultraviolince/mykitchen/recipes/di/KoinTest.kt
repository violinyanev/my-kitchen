package com.ultraviolince.mykitchen.recipes.di

import androidx.lifecycle.SavedStateHandle
import com.ultraviolince.mykitchen.di.appModule
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.KoinTest
import org.koin.test.verify.verify

class KoinTest : KoinTest {

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun checkAllModules() {
        appModule.verify(extraTypes = listOf(SavedStateHandle::class))
    }
}
