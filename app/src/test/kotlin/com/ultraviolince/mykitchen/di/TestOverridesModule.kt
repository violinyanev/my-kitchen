package com.ultraviolince.mykitchen.di

import android.app.Application
import androidx.room.Room
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.RecipeServiceWrapper
import com.ultraviolince.mykitchen.recipes.data.datasource.datastore.SafeDataStore
import com.ultraviolince.mykitchen.recipes.data.datasource.localdb.RecipeDao
import com.ultraviolince.mykitchen.recipes.data.datasource.localdb.RecipeDatabase
import com.ultraviolince.mykitchen.testutil.InMemoryRecipesServer
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class TestOverridesModule {

    @Single
    fun provideInMemoryRecipeDatabase(app: Application): RecipeDatabase {
        return Room.inMemoryDatabaseBuilder(app, RecipeDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @Single
    fun provideRecipeDao(db: RecipeDatabase): RecipeDao = db.recipeDao

    @Single
    fun provideInMemoryServer(): InMemoryRecipesServer = InMemoryRecipesServer()

    @Single
    fun provideRecipeServiceWrapper(
        dao: RecipeDao,
        dataStore: SafeDataStore,
        server: InMemoryRecipesServer
    ): RecipeServiceWrapper {
        return RecipeServiceWrapper(
            dataStore = dataStore,
            dao = dao,
            clientEngineProvider = { server.engine }
        )
    }
}

