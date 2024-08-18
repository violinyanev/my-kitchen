
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import editrecipe.presentation.AddEditRecipeScreen
import login.presentation.LoginScreen
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import org.koin.compose.currentKoinScope
import recipes.presentation.RecipeScreen

@Composable
@Preview
fun App() {
    DisposableEffect(true) {
        Log.i("App composed")
        onDispose {
            Log.i("App decomposed")
        }
    }

    MaterialTheme {
        KoinContext {
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = ScreenRoutes.LoginScreen.route
            ) {
                composable(ScreenRoutes.LoginScreen.route) {
                    val viewModel = koinViewModel<LoginViewModel>()
                    LoginScreen(navController, viewModel)
                }
                composable(ScreenRoutes.RecipesScreen.route) {
                    val viewModel = koinViewModel<RecipeViewModel>()
                    RecipeScreen(navController, viewModel)
                }
                composable(ScreenRoutes.AddEditRecipeScreen.route) {
                    val viewModel = koinViewModel<AddEditRecipeViewModel>()
                    AddEditRecipeScreen(navController, viewModel)
                }
            }
        }
    }
}

@Composable
inline fun <reified T : ViewModel> koinViewModel(): T {
    val scope = currentKoinScope()
    return viewModel {
        scope.get<T>()
    }
}
