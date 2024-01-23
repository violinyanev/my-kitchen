package com.example.myapplication.di

import android.app.Application
import androidx.room.Room
import com.example.myapplication.recipes.data.datasource.backend.RecipeService
import com.example.myapplication.recipes.data.datasource.backend.RecipeServiceWrapper
import com.example.myapplication.recipes.data.datasource.backend.RecipesApiClient
import com.example.myapplication.recipes.data.datasource.localdb.RecipeDatabase
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
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
    fun provideRecipeService(): RecipeServiceWrapper {
        // TODO Make configurable
        val baseUrl = "https://ultraviolince.com:8019"
        return RecipeServiceWrapper(
            Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(RecipeService::class.java)
        )
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
            getRecipes = GetRecipes(repository),
            deleteRecipe = DeleteRecipe(repository),
            addRecipe = AddRecipe(repository),
            getRecipe = GetRecipe(repository)
        )
    }
}
