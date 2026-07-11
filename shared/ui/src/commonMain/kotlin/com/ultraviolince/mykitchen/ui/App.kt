package com.ultraviolince.mykitchen.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.ultraviolince.mykitchen.ui.navigation.Route
import com.ultraviolince.mykitchen.ui.screens.addedit.AddEditScreen
import com.ultraviolince.mykitchen.ui.screens.login.LoginScreen
import com.ultraviolince.mykitchen.ui.screens.recipelist.RecipeListScreen
import com.ultraviolince.mykitchen.ui.theme.AppTheme

data class DefaultCredentials(
    val serverUrl: String? = null,
    val email: String? = null,
    val password: String? = null,
)

@Composable
fun App(defaultCredentials: DefaultCredentials? = null) {
    SingletonImageLoader.setSafe { context ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .build()
    }
    AppTheme {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = Route.RecipeList) {
            composable<Route.RecipeList> {
                RecipeListScreen(
                    onAddRecipe = { navController.navigate(Route.EditRecipe()) },
                    onEditRecipe = { id -> navController.navigate(Route.EditRecipe(id)) },
                    onNavigateToLogin = {
                        navController.navigate(Route.Login) {
                            popUpTo(Route.RecipeList) { inclusive = true }
                        }
                    },
                )
            }
            composable<Route.EditRecipe> { backStackEntry ->
                val route: Route.EditRecipe = backStackEntry.toRoute()
                AddEditScreen(
                    recipeId = route.id,
                    onNavigateBack = { navController.popBackStack() },
                )
            }
            composable<Route.Login> {
                LoginScreen(
                    defaultCredentials = defaultCredentials,
                    onLoginSuccess = {
                        navController.navigate(Route.RecipeList) {
                            popUpTo(Route.Login) { inclusive = true }
                        }
                    },
                )
            }
        }
    }
}

