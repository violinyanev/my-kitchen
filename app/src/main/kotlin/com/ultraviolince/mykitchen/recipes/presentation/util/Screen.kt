package com.ultraviolince.mykitchen.recipes.presentation.util

sealed class Screen(val route: String) {
    object LoginScreen : Screen("login_screen")
    object RecipesScreen : Screen("recipes_screen")
    object AddEditRecipeScreen : Screen("add_edit_recipe_screen")
}
