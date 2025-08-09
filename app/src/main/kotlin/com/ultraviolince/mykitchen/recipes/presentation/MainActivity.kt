package com.ultraviolince.mykitchen.recipes.presentation

import android.os.Bundle
import android.view.Choreographer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ultraviolince.mykitchen.recipes.presentation.editrecipe.AddEditRecipeScreen
import com.ultraviolince.mykitchen.recipes.presentation.login.LoginScreen
import com.ultraviolince.mykitchen.recipes.presentation.recipes.RecipeScreen
import com.ultraviolince.mykitchen.recipes.presentation.util.PerfTracer
import com.ultraviolince.mykitchen.recipes.presentation.util.Screen
import com.ultraviolince.mykitchen.ui.theme.MyApplicationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    LaunchedEffect(Unit) {
        withContext(Dispatchers.Main) {
            Choreographer.getInstance().postFrameCallback {
                PerfTracer.endAsyncSection("AppStartup")
            }
        }
    }

    Surface(
        modifier = modifier.safeDrawingPadding().fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = Screen.RecipesScreen.route,
            modifier = Modifier.semantics {
                role = Role.Button
                contentDescription = "Main navigation"
            }
        ) {
            composable(route = Screen.LoginScreen.route) {
                LoginScreen(navController = navController)
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
