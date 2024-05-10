package com.ultraviolince.mykitchen.di

import com.ultraviolince.mykitchen.recipes.data.datasource.backend.RecipeServiceWrapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
open class BackendModule {

    @Provides
    @Singleton
    fun provideRecipeServiceWrapper(): RecipeServiceWrapper {
        return RecipeServiceWrapper()
    }
}
