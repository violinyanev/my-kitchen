package com.ultraviolince.mykitchen.recipes.domain.usecase

data class Recipes(
    val createUser: CreateUser,
    val getUserById: GetUser,
    val getUsers: GetUsers,
    val getDefaultUser: GetDefaultUser,
    val login: Login,
    val getSyncState: GetLoginState,
    val getRecipes: GetRecipes,
    val deleteRecipe: DeleteRecipe,
    val addRecipe: AddRecipe,
    val getRecipe: GetRecipe
)
