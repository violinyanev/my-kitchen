package com.ultraviolince.mykitchen.ui.di

import com.ultraviolince.mykitchen.ui.screens.addedit.AddEditViewModel
import com.ultraviolince.mykitchen.ui.screens.login.LoginViewModel
import com.ultraviolince.mykitchen.ui.screens.recipelist.RecipeListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val uiModule = module {
    viewModelOf(::RecipeListViewModel)
    viewModelOf(::LoginViewModel)
    viewModel { (recipeId: String?) ->
        AddEditViewModel(get(), get(), get(), recipeId)
    }
}
