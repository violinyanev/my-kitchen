package com.ultraviolince.mykitchen.data.di

import com.ultraviolince.mykitchen.data.local.InMemoryRecipeDao
import com.ultraviolince.mykitchen.data.local.RecipeDao
import org.koin.dsl.module

actual val platformDataModule = module {
    single<RecipeDao> { InMemoryRecipeDao() }
}
