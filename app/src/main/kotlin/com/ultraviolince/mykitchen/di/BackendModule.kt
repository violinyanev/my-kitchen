package com.ultraviolince.mykitchen.di

import com.ultraviolince.mykitchen.recipes.data.datasource.backend.RecipeServiceWrapper
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class BackendModule {

    @Single
    fun provideRecipeServiceWrapper(): RecipeServiceWrapper {
        return RecipeServiceWrapper()
    }
}
