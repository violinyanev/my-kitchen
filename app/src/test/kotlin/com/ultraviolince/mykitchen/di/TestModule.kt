package com.ultraviolince.mykitchen.di

import androidx.lifecycle.SavedStateHandle
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class TestModule {
    @Single
    fun provideSavedStateHandle(): SavedStateHandle {
        return SavedStateHandle()
    }
}
