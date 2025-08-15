package com.ultraviolince.mykitchen.recipes.domain.usecase



data class Recipes(
    val login: Login,
    val logout: Logout,
    val getSyncState: GetLoginState,
    val getRecipes: GetRecipes,
    val deleteRecipe: DeleteRecipe,
    val addRecipe: AddRecipe,
    val getRecipe: GetRecipe
)
