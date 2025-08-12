package com.ultraviolince.mykitchen.di

import android.app.Application
import androidx.room.Room
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.RecipeServiceWrapper
import com.ultraviolince.mykitchen.recipes.data.datasource.datastore.SafeDataStore
import com.ultraviolince.mykitchen.recipes.data.datasource.localdb.RecipeDao
import com.ultraviolince.mykitchen.recipes.data.datasource.localdb.RecipeDatabase
import com.ultraviolince.mykitchen.recipes.data.repository.RecipeRepositoryImpl
import com.ultraviolince.mykitchen.recipes.domain.repository.RecipeRepository
import com.ultraviolince.mykitchen.recipes.domain.usecase.AddRecipe
import com.ultraviolince.mykitchen.recipes.domain.usecase.DeleteRecipe
import com.ultraviolince.mykitchen.recipes.domain.usecase.GetLoginState
import com.ultraviolince.mykitchen.recipes.domain.usecase.GetRecipe
import com.ultraviolince.mykitchen.recipes.domain.usecase.GetRecipes
import com.ultraviolince.mykitchen.recipes.domain.usecase.Login
import com.ultraviolince.mykitchen.recipes.domain.usecase.Logout
import com.ultraviolince.mykitchen.recipes.domain.usecase.Recipes
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideRecipeDatabase(app: Application): RecipeDatabase {
        return Room.databaseBuilder(
            app,
            RecipeDatabase::class.java,
            RecipeDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideRecipeDao(db: RecipeDatabase): RecipeDao {
        return db.recipeDao
    }

    @Provides
    @Singleton
    fun provideSafeDataStore(app: Application): SafeDataStore {
        return SafeDataStore(app)
    }

    @Provides
    @Singleton
    fun provideRecipeRepository(
        dao: RecipeDao,
        service: RecipeServiceWrapper,
    ): RecipeRepository {
        return RecipeRepositoryImpl(dao, service)
    }

    @Provides
    @Singleton
    fun provideRecipesUseCases(repository: RecipeRepository): Recipes {
        return Recipes(
            login = Login(repository),
            logout = Logout(repository),
            getSyncState = GetLoginState(repository),
            getRecipes = GetRecipes(repository),
            deleteRecipe = DeleteRecipe(repository),
            addRecipe = AddRecipe(repository),
            getRecipe = GetRecipe(repository)
        )
    }

    @Provides
    @Singleton
    fun provideRecipeServiceWrapper(dao: RecipeDao, dataStore: SafeDataStore): RecipeServiceWrapper {
        return RecipeServiceWrapper(dataStore, dao)
    }
}
