package com.ultraviolince.mykitchen.data.di

import android.content.Context
import com.ultraviolince.mykitchen.data.local.RecipeDatabase
import com.ultraviolince.mykitchen.data.local.getDatabaseBuilder
import org.koin.dsl.module

actual val platformDataModule = module {
    single { getDatabaseBuilder(get<Context>()).build() }
    single { get<RecipeDatabase>().recipeDao() }
}
