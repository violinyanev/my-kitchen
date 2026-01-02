package com.ultraviolince.mykitchen.di

import android.app.Application
import androidx.room.Room
import com.ultraviolince.mykitchen.recipes.data.datasource.datastore.SafeDataStore
import com.ultraviolince.mykitchen.recipes.data.datasource.localdb.RecipeDao
import com.ultraviolince.mykitchen.recipes.data.datasource.localdb.RecipeDatabase
import com.ultraviolince.mykitchen.recipes.data.repository.AuthRepositoryImpl
import com.ultraviolince.mykitchen.recipes.data.repository.RecipeRepositoryImpl
import com.ultraviolince.mykitchen.recipes.data.service.AuthServiceImpl
import com.ultraviolince.mykitchen.recipes.data.service.NetworkServiceImpl
import com.ultraviolince.mykitchen.recipes.data.service.RecipeNetworkServiceImpl
import com.ultraviolince.mykitchen.recipes.domain.repository.AuthRepository
import com.ultraviolince.mykitchen.recipes.domain.repository.RecipeRepository
import com.ultraviolince.mykitchen.recipes.domain.service.AuthService
import com.ultraviolince.mykitchen.recipes.domain.service.NetworkService
import com.ultraviolince.mykitchen.recipes.domain.service.RecipeNetworkService
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
        ).build()
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
    fun provideNetworkService(): NetworkService {
        return NetworkServiceImpl()
    }

    @Single
    fun provideAuthService(dataStore: SafeDataStore, networkService: NetworkService): AuthService {
        return AuthServiceImpl(dataStore, networkService)
    }

    @Single
    fun provideAuthRepository(authService: AuthService): AuthRepository {
        return AuthRepositoryImpl(authService)
    }

    @Single
    fun provideRecipeNetworkService(
        dataStore: SafeDataStore,
        dao: RecipeDao,
        networkService: NetworkService
    ): RecipeNetworkService {
        return RecipeNetworkServiceImpl(dataStore, dao, networkService)
    }

    @Single
    fun provideRecipeRepository(
        dao: RecipeDao,
        recipeNetworkService: RecipeNetworkService,
    ): RecipeRepository {
        return RecipeRepositoryImpl(dao, recipeNetworkService)
    }

    @Single
    fun provideLoginUseCase(authRepository: AuthRepository): Login {
        return Login(authRepository)
    }

    @Single
    fun provideLogoutUseCase(authRepository: AuthRepository): Logout {
        return Logout(authRepository)
    }

    @Single
    fun provideGetLoginStateUseCase(authRepository: AuthRepository): GetLoginState {
        return GetLoginState(authRepository)
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
}
