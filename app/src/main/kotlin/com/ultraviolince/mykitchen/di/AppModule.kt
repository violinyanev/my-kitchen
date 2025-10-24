package com.ultraviolince.mykitchen.di

import android.app.Application
import androidx.room.Room
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.RecipeServiceWrapper
import com.ultraviolince.mykitchen.recipes.data.datasource.datastore.SafeDataStore
import com.ultraviolince.mykitchen.recipes.data.datasource.localdb.MIGRATION_1_2
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
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan("com.ultraviolince.mykitchen")
class AppModule {
    @Single
    fun provideRecipeDatabase(app: Application): RecipeDatabase {
        return Room.databaseBuilder(
            app,
            RecipeDatabase::class.java,
            RecipeDatabase.DATABASE_NAME
        ).addMigrations(MIGRATION_1_2).build()
    }

    @Single
    fun provideRecipeDao(db: RecipeDatabase): RecipeDao {
        return db.recipeDao
    }

    @Single
    fun provideSafeDataStore(app: Application): SafeDataStore {
        return SafeDataStore(app)
    }

    @Single
    fun provideRecipeRepository(
        dao: RecipeDao,
        service: RecipeServiceWrapper,
    ): RecipeRepository {
        return RecipeRepositoryImpl(dao, service)
    }

    @Single
    fun provideLoginUseCase(repository: RecipeRepository): Login {
        return Login(repository)
    }

    @Single
    fun provideLogoutUseCase(repository: RecipeRepository): Logout {
        return Logout(repository)
    }

    @Single
    fun provideGetLoginStateUseCase(repository: RecipeRepository): GetLoginState {
        return GetLoginState(repository)
    }

    @Single
    fun provideGetRecipesUseCase(repository: RecipeRepository): GetRecipes {
        return GetRecipes(repository)
    }

    @Single
    fun provideDeleteRecipeUseCase(repository: RecipeRepository): DeleteRecipe {
        return DeleteRecipe(repository)
    }

    @Single
    fun provideAddRecipeUseCase(repository: RecipeRepository): AddRecipe {
        return AddRecipe(repository)
    }

    @Single
    fun provideGetRecipeUseCase(repository: RecipeRepository): GetRecipe {
        return GetRecipe(repository)
    }

    @Single
    fun provideRecipesUseCases(
        login: Login,
        logout: Logout,
        getSyncState: GetLoginState,
        getRecipes: GetRecipes,
        deleteRecipe: DeleteRecipe,
        addRecipe: AddRecipe,
        getRecipe: GetRecipe
    ): Recipes {
        return Recipes(
            login = login,
            logout = logout,
            getSyncState = getSyncState,
            getRecipes = getRecipes,
            deleteRecipe = deleteRecipe,
            addRecipe = addRecipe,
            getRecipe = getRecipe
        )
    }

    @Single
    fun provideRecipeServiceWrapper(dao: RecipeDao, dataStore: SafeDataStore): RecipeServiceWrapper {
        return RecipeServiceWrapper(dataStore, dao)
    }
}
