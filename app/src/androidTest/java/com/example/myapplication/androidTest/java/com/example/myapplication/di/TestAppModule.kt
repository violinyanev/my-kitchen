package com.example.myapplication.di

import android.app.Application
import androidx.room.Room
import com.example.myapplication.recipes.data.datasource.RecipeDatabase
import com.example.myapplication.recipes.data.repository.RecipeRepositoryImpl
import com.example.myapplication.recipes.domain.repository.RecipeRepository
import com.example.myapplication.recipes.domain.usecase.AddRecipe
import com.example.myapplication.recipes.domain.usecase.DeleteRecipe
import com.example.myapplication.recipes.domain.usecase.GetRecipe
import com.example.myapplication.recipes.domain.usecase.GetRecipes
import com.example.myapplication.recipes.domain.usecase.Recipes
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TestAppModule {

    @Provides
    @Singleton
    fun provideRecipeDatabase(app: Application): RecipeDatabase {
        return Room.inMemoryDatabaseBuilder(
            app,
            RecipeDatabase::class.java
        ).build()
    }

    @Provides
    @Singleton
    fun provideRecipeRepository(db: RecipeDatabase): RecipeRepository {
        return RecipeRepositoryImpl(db.recipeDao)
    }

    @Provides
    @Singleton
    fun provideRecipeUseCases(repository: RecipeRepository): Recipes {
        return Recipes(
            getRecipes = GetRecipes(repository),
            deleteRecipe = DeleteRecipe(repository),
            addRecipe = AddRecipe(repository),
            getRecipe = GetRecipe(repository)
        )
    }
}
