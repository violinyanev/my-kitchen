package recipes.presentation

import RecipeViewModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import domain.model.Recipe
import domain.repository.LoginState
import mykitchen.composeapp.generated.resources.Res
import mykitchen.composeapp.generated.resources.add_recipe
import mykitchen.composeapp.generated.resources.sort
import mykitchen.composeapp.generated.resources.sync_disabled
import mykitchen.composeapp.generated.resources.sync_enabled
import mykitchen.composeapp.generated.resources.sync_loading
import mykitchen.composeapp.generated.resources.your_recipes
import org.jetbrains.compose.resources.stringResource
import recipes.presentation.components.OrderSection
import recipes.presentation.components.RecipeItem

@Composable
fun RecipeScreen(
    navController: NavController,
    viewModel: RecipeViewModel, // TODO kmp  = koinViewModel()
) {
    val state = viewModel.state.value

    RecipeScreenContent(
        onAddRecipe = {
            navController.navigate(ScreenRoutes.AddEditRecipeScreen.route)
        },
        onLoginClick = {
            navController.navigate(
                ScreenRoutes.LoginScreen.route, // TODO fix?
            )
        },
        onSortClick = {
            viewModel.onEvent(RecipesEvent.ToggleOrderSection)
        },
        onEvent = {
            viewModel.onEvent(it)
        },
        onRecipeClicked = { recipe ->
            val route = ScreenRoutes.AddEditRecipeScreen.route + "?recipeId=${recipe.id}"
            Log.d("Navigating to $route")
            navController.navigate(
                route,
            )
        },
        recipeState = state,
    )
}

@Composable
private fun RecipeScreenContent(
    onAddRecipe: () -> Unit,
    onSortClick: () -> Unit,
    onLoginClick: () -> Unit,
    onEvent: (RecipesEvent) -> Unit,
    onRecipeClicked: (Recipe) -> Unit,
    recipeState: RecipesState,
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddRecipe,
                modifier = Modifier.semantics { contentDescription = "New recipe" },
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(resource = Res.string.add_recipe),
                )
            }
        },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.your_recipes),
                    style = MaterialTheme.typography.body1,
                )
                IconButton(
                    onClick = onSortClick,
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = stringResource(resource = Res.string.sort),
                    )
                }
                IconButton(
                    onClick = onLoginClick,
                ) {
                    when (recipeState.syncState) {
                        LoginState.LoginEmpty ->
                            Icon(
                                imageVector = Icons.Default.SyncProblem,
                                contentDescription = stringResource(resource = Res.string.sync_disabled),
                            )

                        LoginState.LoginPending ->
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = stringResource(resource = Res.string.sync_loading),
                            )

                        LoginState.LoginSuccess ->
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = stringResource(resource = Res.string.sync_enabled),
                            )

                        is LoginState.LoginFailure ->
                            Icon(
                                imageVector = Icons.Default.SyncProblem,
                                contentDescription = stringResource(resource = Res.string.sync_disabled),
                            )
                    }
                }
            }
            AnimatedVisibility(
                visible = recipeState.isOrderSelectionVisible,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically(),
            ) {
                OrderSection(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                    recipeOrder = recipeState.recipeOrder,
                    onOrderChange = {
                        onEvent(RecipesEvent.Order(it))
                    },
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn {
                items(recipeState.recipes) { recipe ->
                    RecipeItem(
                        recipe = recipe,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onRecipeClicked(recipe)
                                },
                    )
                }
            }
        }
    }
}

// class RecipeScreenStatePreviewParameterProvider : PreviewParameterProvider<RecipesState> {
//    override val values = sequenceOf(
//        RecipesState(),
//        RecipesState(syncState = LoginState.LoginEmpty),
//        RecipesState(syncState = LoginState.LoginPending),
//        RecipesState(syncState = LoginState.LoginSuccess),
//        RecipesState(syncState = LoginState.LoginFailure(error = NetworkError.UNAUTHORIZED)),
//        RecipesState(
//            recipes = List(10) { index ->
//                Recipe(
//                    "Recipe $index",
//                    content = "Lorem ipsum dolor sit amet $index",
//                    timestamp = 5
//                )
//            }
//        )
//    )
// }
//
// @Preview(showBackground = true, showSystemUi = true)
// @Composable
// private fun RecipeScreenPreviewRealistic(
//    @PreviewParameter(RecipeScreenStatePreviewParameterProvider::class) recipesState: RecipesState
// ) {
//    MyApplicationTheme {
//        RecipeScreenContent(
//            onAddRecipe = {},
//            onLoginClick = {},
//            onSortClick = {},
//            onEvent = {},
//            onRecipeClicked = {},
//            recipeState = recipesState
//        )
//    }
// }
