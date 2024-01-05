package com.example.myapplication.recipes.presentation

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
import com.example.myapplication.recipes.presentation.editrecipe.AddEditRecipeScreen
import com.example.myapplication.recipes.presentation.recipes.RecipeScreen
import com.example.myapplication.recipes.presentation.util.Screen
import com.example.myapplication.ui.theme.MyApplicationTheme
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
fun MainScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = Screen.RecipesScreen.route
        ) {
            composable(route = Screen.RecipesScreen.route) {
                RecipeScreen(navController = navController)
            }
            composable(
                route = Screen.AddEditRecipeScreen.route + "?recipeId={recipeId}&recipeColor={recipeColor}",
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
