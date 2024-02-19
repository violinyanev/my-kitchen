package com.example.myapplication.di

import android.app.Application
import androidx.room.Room
import com.example.myapplication.recipes.data.datasource.backend.RecipeServiceWrapper
import com.example.myapplication.recipes.data.datasource.localdb.RecipeDatabase
import com.example.myapplication.recipes.data.repository.RecipeRepositoryImpl
import com.example.myapplication.recipes.domain.repository.RecipeRepository
import com.example.myapplication.recipes.domain.usecase.AddRecipe
import com.example.myapplication.recipes.domain.usecase.DeleteRecipe
import com.example.myapplication.recipes.domain.usecase.GetLoginState
import com.example.myapplication.recipes.domain.usecase.GetRecipe
import com.example.myapplication.recipes.domain.usecase.GetRecipes
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
    fun provideRecipeRepository(
        db: RecipeDatabase
    ): RecipeRepository {
        return RecipeRepositoryImpl(db.recipeDao, RecipeServiceWrapper())
    }

    @Provides
    @Singleton
    fun provideRecipesUseCases(repository: RecipeRepository): Recipes {
        return Recipes(
            login = Login(repository),
            getSyncState = GetLoginState(repository),
            getRecipes = GetRecipes(repository),
            deleteRecipe = DeleteRecipe(repository),
            addRecipe = AddRecipe(repository),
            getRecipe = GetRecipe(repository)
        )
    }
}
