package com.example.myapplication.di

import android.app.Application
import androidx.room.Room
import com.example.myapplication.recipes.data.datasource.backend.RecipeService
import com.example.myapplication.recipes.data.datasource.backend.RecipeServiceWrapper
import com.example.myapplication.recipes.data.datasource.localdb.RecipeDatabase
import com.example.myapplication.recipes.data.repository.RecipeRepositoryImpl
import com.example.myapplication.recipes.domain.repository.RecipeRepository
import com.example.myapplication.recipes.domain.usecase.AddRecipe
import com.example.myapplication.recipes.domain.usecase.DeleteRecipe
import com.example.myapplication.recipes.domain.usecase.GetRecipe
import com.example.myapplication.recipes.domain.usecase.GetRecipes
import com.example.myapplication.recipes.domain.usecase.GetSyncState
import com.example.myapplication.recipes.domain.usecase.Login
import com.example.myapplication.recipes.domain.usecase.Recipes
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
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
    fun provideRecipeService(db: RecipeDatabase): RecipeServiceWrapper {
        return RecipeServiceWrapper(db.recipeDao)
    }

    @Provides
    @Singleton
    fun provideRecipeRepository(db: RecipeDatabase, service: RecipeServiceWrapper): RecipeRepository {
        return RecipeRepositoryImpl(db.recipeDao, service)
    }

    @Provides
    @Singleton
    fun provideRecipesUseCases(repository: RecipeRepository): Recipes {
        return Recipes(
            login = Login(repository),
            getSyncState = GetSyncState(repository),
            getRecipes = GetRecipes(repository),
            deleteRecipe = DeleteRecipe(repository),
            addRecipe = AddRecipe(repository),
            getRecipe = GetRecipe(repository)
        )
    }
}
