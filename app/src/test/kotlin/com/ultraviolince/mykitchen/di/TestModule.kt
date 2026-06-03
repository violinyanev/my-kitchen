package com.ultraviolince.mykitchen.di

import androidx.lifecycle.SavedStateHandle
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.core.module.Module as KoinModule
import org.koin.dsl.module

@Module
class TestModule {
    @Single
    fun provideSavedStateHandle(): SavedStateHandle {
        return SavedStateHandle()
    }
}

val testModule: KoinModule = module {
    single { SavedStateHandle() }
}
