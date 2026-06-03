package com.ultraviolince.mykitchen.di

import android.app.Application
import androidx.room.Room
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.RecipeServiceWrapper
import com.ultraviolince.mykitchen.recipes.data.datasource.datastore.SafeDataStore
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
import com.ultraviolince.mykitchen.recipes.presentation.editrecipe.AddEditRecipeViewModel
import com.ultraviolince.mykitchen.recipes.presentation.login.LoginViewModel
import com.ultraviolince.mykitchen.recipes.presentation.recipes.RecipeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.Module
import org.koin.dsl.module

val appModule: Module = module {
    single { Room.databaseBuilder(get<Application>(), RecipeDatabase::class.java, RecipeDatabase.DATABASE_NAME).build() }
    single { get<RecipeDatabase>().recipeDao }
    single { SafeDataStore(get()) }
    single { RecipeServiceWrapper(get(), get()) }
    single<RecipeRepository> { RecipeRepositoryImpl(get(), get()) }
    single { Login(get()) }
    single { Logout(get()) }
    single { GetLoginState(get()) }
    single { GetRecipes(get()) }
    single { DeleteRecipe(get()) }
    single { AddRecipe(get()) }
    single { GetRecipe(get()) }
    single {
        Recipes(
            login = get(),
            logout = get(),
            getSyncState = get(),
            getRecipes = get(),
            deleteRecipe = get(),
            addRecipe = get(),
            getRecipe = get(),
        )
    }
    viewModelOf(::LoginViewModel)
    viewModelOf(::RecipeViewModel)
    viewModelOf(::AddEditRecipeViewModel)
}
