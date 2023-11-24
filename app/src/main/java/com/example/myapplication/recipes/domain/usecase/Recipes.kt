package com.example.myapplication.recipes.domain.usecase

data class Recipes(
    val getRecipes: GetRecipes,
    val deleteRecipe: DeleteRecipe,
    val addRecipe: AddRecipe,
    val getRecipe: GetRecipe
)
