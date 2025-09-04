package com.ultraviolince.mykitchen.di

import com.ultraviolince.mykitchen.recipes.data.datasource.backend.JsRecipeServiceWrapper
import com.ultraviolince.mykitchen.recipes.data.datasource.datastore.LocalStorageDataStore
import com.ultraviolince.mykitchen.recipes.data.datasource.localstorage.LocalStorageRecipeDao
import com.ultraviolince.mykitchen.recipes.data.repository.JsRecipeRepositoryImpl
import com.ultraviolince.mykitchen.recipes.domain.repository.RecipeRepository
import com.ultraviolince.mykitchen.recipes.domain.usecase.*
import org.koin.dsl.module

val jsAppModule = module {
    // Data layer
    single { LocalStorageRecipeDao() }
    single { LocalStorageDataStore() }
    single { JsRecipeServiceWrapper() }
    
    // Repository
    single<RecipeRepository> { 
        JsRecipeRepositoryImpl(get(), get(), get()) 
    }
    
    // Use cases
    single { GetRecipes(get()) }
    single { AddRecipe(get()) }
    single { DeleteRecipe(get()) }
    single { GetRecipe(get()) }
    single { Login(get()) }
    single { Logout(get()) }
    single { GetLoginState(get()) }
    
    // Use case bundle
    single { 
        Recipes(
            login = get(),
            logout = get(),
            getSyncState = get(),
            getRecipes = get(),
            deleteRecipe = get(),
            addRecipe = get(),
            getRecipe = get()
        )
    }
}