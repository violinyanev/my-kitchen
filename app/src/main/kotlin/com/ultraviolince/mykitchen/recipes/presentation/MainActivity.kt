package com.ultraviolince.mykitchen.recipes.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ultraviolince.mykitchen.recipes.presentation.editrecipe.AddEditRecipeScreen
import com.ultraviolince.mykitchen.recipes.presentation.login.CreateUserScreen
import com.ultraviolince.mykitchen.recipes.presentation.login.LoginScreen
import com.ultraviolince.mykitchen.recipes.presentation.recipes.RecipeScreen
import com.ultraviolince.mykitchen.recipes.presentation.util.Screen
import com.ultraviolince.mykitchen.ui.theme.MyApplicationTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = Screen.LoginScreen.route
        ) {
            composable(route = Screen.LoginScreen.route) {
                LoginScreen(navController = navController)
            }
            composable(route = Screen.CreateUserScreen.route) {
                CreateUserScreen(navController = navController)
            }
            composable(route = Screen.RecipesScreen.route) {
                RecipeScreen(navController = navController)
            }
            composable(
                route = Screen.AddEditRecipeScreen.route + "?recipeId={recipeId}",
                arguments = listOf(
                    navArgument(name = "recipeId") {
                        type = NavType.IntType
                        defaultValue = -1
                    }
                )
            ) {
                AddEditRecipeScreen(
                    navController = navController
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        MainScreen()
    }
}
