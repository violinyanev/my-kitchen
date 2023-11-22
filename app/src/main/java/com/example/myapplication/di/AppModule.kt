package com.example.myapplication.di

import android.app.Application
import androidx.room.Room
import com.example.myapplication.recipes.data.datasource.RecipeDatabase
import com.example.myapplication.recipes.domain.repository.RecipeRepository
import com.example.myapplication.recipes.domain.repository.RecipeRepositoryImpl
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
    fun provideRecipeRepository(db: RecipeDatabase): RecipeRepository {
        return RecipeRepositoryImpl(db.recipeDao)
    }

    @Provides
    @Singleton
    fun provideRecipesUseCases(repository: RecipeRepository): Recipes {
        return Recipes(
            getRecipes = GetRecipes(repository),
            deleteRecipe = DeleteRecipe(repository),
            addRecipe = AddRecipe(repository),
            getRecipe = GetRecipe(repository)
        )
    }

    /*@Provides
    @Singleton
    fun provideRecipeRepository(api: RecipeAPI): RecipeRepository {
        return RecipeRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideRecipeAPI(): RecipeAPI {
        return Retrofit.Builder()
            .baseUrl("http://127.0.0.1:5000/recipes")  // TODO make configurable
            .build()
            .create(RecipeAPI::class.java)
    }

    @Provides
    @Singleton
    fun provideRecipeDatabase(): RecipeDatabase {
        return Retrofit.Builder()
            .baseUrl("http://127.0.0.1:5000/recipes")  // TODO make configurable
            .build()
            .create(RecipeAPI::class.java)
    }*/
}
