package com.ultraviolince.mykitchen.data.di

import android.content.Context
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.ultraviolince.mykitchen.data.local.RecipeDao
import com.ultraviolince.mykitchen.data.local.RecipeDatabase
import com.ultraviolince.mykitchen.data.local.RoomRecipeDaoAdapter
import com.ultraviolince.mykitchen.data.local.getDatabaseBuilder
import com.ultraviolince.mykitchen.data.store.CredentialsStore
import com.ultraviolince.mykitchen.data.store.SharedPreferencesCredentialsStore
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

actual val platformDataModule = module {
    single {
        getDatabaseBuilder(get<Context>())
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
    single<RecipeDao> { RoomRecipeDaoAdapter(get<RecipeDatabase>().recipeRoomDao()) }
    single<CredentialsStore> { SharedPreferencesCredentialsStore(get()) }
}

