
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import editrecipe.presentation.AddEditRecipeScreen
import login.presentation.LoginScreen
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import org.koin.compose.currentKoinScope
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.parameter.parametersOf
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
                composable(route = ScreenRoutes.LoginScreen.route) {
                    val viewModel = koinViewModel<LoginViewModel>()
                    LoginScreen(navController, viewModel)
                }
                composable(route = ScreenRoutes.RecipesScreen.route) {
                    val viewModel = koinViewModel<RecipeViewModel>()
                    RecipeScreen(navController, viewModel)
                }
                composable(
                    route = ScreenRoutes.AddEditRecipeScreen.route + "?recipeId={recipeId}",
                    arguments = listOf(
                        navArgument(name = "recipeId") {
                            type = NavType.LongType
                            defaultValue = -1L
                        }
                    )
                ) { backStackEntry ->
                    val recipeId = backStackEntry.arguments?.getLong("recipeId")
                    val viewModel = koinViewModel<AddEditRecipeViewModel>() {
                        parametersOf(recipeId)
                    }
                    AddEditRecipeScreen(navController, viewModel)
                }
            }
        }
    }
}

@Composable
inline fun <reified T : ViewModel> koinViewModel(noinline parameters: ParametersDefinition? = null): T {
    val scope = currentKoinScope()
    return viewModel {
        scope.get<T>(parameters = parameters)
    }
}
