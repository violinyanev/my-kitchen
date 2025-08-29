package com.ultraviolince.mykitchen.recipes.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ultraviolince.mykitchen.recipes.presentation.editrecipe.AddEditRecipeScreen
import com.ultraviolince.mykitchen.recipes.presentation.login.LoginScreen
import com.ultraviolince.mykitchen.recipes.presentation.recipes.RecipeScreen

/**
 * Navigation routes for the app
 */
object NavigationRoutes {
    const val LOGIN = "login"
    const val RECIPES = "recipes"
    const val ADD_EDIT_RECIPE = "add_edit_recipe"
    
    // Arguments
    const val RECIPE_ID_ARG = "recipeId"
}

/**
 * Navigation arguments
 */
object NavigationArgs {
    val recipeId = navArgument(NavigationRoutes.RECIPE_ID_ARG) {
        type = NavType.IntType
        defaultValue = -1
    }
}

/**
 * Builds the navigation graph for the app
 */
fun NavGraphBuilder.buildNavigationGraph(navController: NavHostController) {
    composable(NavigationRoutes.LOGIN) {
        LoginScreen(navController = navController)
    }
    
    composable(NavigationRoutes.RECIPES) {
        RecipeScreen(navController = navController)
    }
    
    composable(
        route = "${NavigationRoutes.ADD_EDIT_RECIPE}/{${NavigationRoutes.RECIPE_ID_ARG}}",
        arguments = listOf(NavigationArgs.recipeId)
    ) { backStackEntry ->
        val recipeId = backStackEntry.arguments?.getInt(NavigationRoutes.RECIPE_ID_ARG) ?: -1
        AddEditRecipeScreen(
            navController = navController,
            recipeId = recipeId
        )
    }
}

/**
 * Navigation actions that can be performed from any screen
 */
class NavigationActions(private val navController: NavHostController) {
    
    fun navigateToLogin() {
        navController.navigate(NavigationRoutes.LOGIN) {
            popUpTo(0) { inclusive = true }
        }
    }
    
    fun navigateToRecipes() {
        navController.navigate(NavigationRoutes.RECIPES) {
            popUpTo(NavigationRoutes.LOGIN) { inclusive = true }
        }
    }
    
    fun navigateToAddRecipe() {
        navController.navigate("${NavigationRoutes.ADD_EDIT_RECIPE}/-1")
    }
    
    fun navigateToEditRecipe(recipeId: Int) {
        navController.navigate("${NavigationRoutes.ADD_EDIT_RECIPE}/$recipeId")
    }
    
    fun navigateBack() {
        navController.popBackStack()
    }
    
    fun navigateToRecipesAndClearStack() {
        navController.navigate(NavigationRoutes.RECIPES) {
            popUpTo(0) { inclusive = true }
        }
    }
}