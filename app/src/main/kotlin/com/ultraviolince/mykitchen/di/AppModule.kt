package com.ultraviolince.mykitchen.di

import android.app.Application
import androidx.room.Room
import com.example.myapplication.recipes.data.util.DatabaseInitializer
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.RecipeServiceWrapper
import com.ultraviolince.mykitchen.recipes.data.datasource.localdb.RecipeDatabase
import com.ultraviolince.mykitchen.recipes.data.repository.RecipeRepositoryImpl
import com.ultraviolince.mykitchen.recipes.domain.repository.RecipeRepository
import com.ultraviolince.mykitchen.recipes.domain.usecase.AddRecipe
import com.ultraviolince.mykitchen.recipes.domain.usecase.DeleteRecipe
import com.ultraviolince.mykitchen.recipes.domain.usecase.GetLoginState
import com.ultraviolince.mykitchen.recipes.domain.usecase.GetRecipe
import com.ultraviolince.mykitchen.recipes.domain.usecase.GetRecipes
import com.ultraviolince.mykitchen.recipes.domain.usecase.Login
import com.ultraviolince.mykitchen.recipes.domain.usecase.Recipes
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    @Singleton
    fun provideRecipeDatabase(app: Application): RecipeDatabase {
        val db = Room.databaseBuilder(
            app,
            RecipeDatabase::class.java,
            RecipeDatabase.DATABASE_NAME
        ).build()

        if (BuildConfig.DEBUG) {
            runBlocking {
                DatabaseInitializer.populateDatabase(db)
            }
        }

        return db
    }

    @Provides
    @Singleton
    fun provideRecipeRepository(
        db: RecipeDatabase,
        service: RecipeServiceWrapper
    ): RecipeRepository {
        return RecipeRepositoryImpl(db.recipeDao, service)
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
