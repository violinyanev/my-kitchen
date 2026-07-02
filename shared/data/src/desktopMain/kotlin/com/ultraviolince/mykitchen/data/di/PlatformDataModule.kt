package com.ultraviolince.mykitchen.data.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.ultraviolince.mykitchen.data.local.RecipeDao
import com.ultraviolince.mykitchen.data.local.RecipeDatabase
import com.ultraviolince.mykitchen.data.local.RoomRecipeDaoAdapter
import com.ultraviolince.mykitchen.data.local.getDatabaseBuilder
import com.ultraviolince.mykitchen.data.store.CredentialsStore
import com.ultraviolince.mykitchen.data.store.DesktopCredentialsStore
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

actual val platformDataModule = module {
    single {
        getDatabaseBuilder()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
    single<RecipeDao> { RoomRecipeDaoAdapter(get<RecipeDatabase>().recipeRoomDao()) }
    single<CredentialsStore> { DesktopCredentialsStore() }
}

