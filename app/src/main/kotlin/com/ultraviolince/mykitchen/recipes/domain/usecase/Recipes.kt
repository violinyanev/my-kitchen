package com.ultraviolince.mykitchen.recipes.domain.usecase

import org.koin.core.annotation.Single

@Single
data class Recipes(
    val login: Login,
    val getSyncState: GetLoginState,
    val getRecipes: GetRecipes,
    val deleteRecipe: DeleteRecipe,
    val addRecipe: AddRecipe,
    val getRecipe: GetRecipe
)
