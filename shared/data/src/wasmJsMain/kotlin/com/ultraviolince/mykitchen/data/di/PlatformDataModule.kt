package com.ultraviolince.mykitchen.data.di

import com.ultraviolince.mykitchen.data.local.InMemoryRecipeDao
import com.ultraviolince.mykitchen.data.local.RecipeDao
import com.ultraviolince.mykitchen.data.store.CredentialsStore
import com.ultraviolince.mykitchen.data.store.WebCredentialsStore
import org.koin.dsl.module

actual val platformDataModule = module {
    single<RecipeDao> { InMemoryRecipeDao() }
    single<CredentialsStore> { WebCredentialsStore() }
}
