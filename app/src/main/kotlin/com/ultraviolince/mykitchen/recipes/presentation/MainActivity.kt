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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.ultraviolince.mykitchen.recipes.presentation.editrecipe.AddEditRecipeScreen
import com.ultraviolince.mykitchen.recipes.presentation.login.LoginScreen
import com.ultraviolince.mykitchen.recipes.presentation.recipes.RecipeScreen
import com.ultraviolince.mykitchen.recipes.presentation.util.AddEditRecipeScreenTarget
import com.ultraviolince.mykitchen.recipes.presentation.util.LoginScreenTarget
import com.ultraviolince.mykitchen.recipes.presentation.util.PerfTracer
import com.ultraviolince.mykitchen.recipes.presentation.util.RecipesScreenTarget
import com.ultraviolince.mykitchen.ui.theme.MyApplicationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel

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
            startDestination = RecipesScreenTarget
        ) {
            composable<LoginScreenTarget> {
                LoginScreen(navController = navController)
            }
            composable<RecipesScreenTarget> {
                RecipeScreen(navController = navController)
            }
            composable<AddEditRecipeScreenTarget>
            {
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
