package com.ultraviolince.mykitchen.recipes.presentation.recipes

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ultraviolince.mykitchen.R
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.util.NetworkError
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
import com.ultraviolince.mykitchen.recipes.presentation.recipes.components.OrderSection
import com.ultraviolince.mykitchen.recipes.presentation.util.Screen
import com.ultraviolince.mykitchen.ui.theme.MyApplicationTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun RecipeScreen(
    navController: NavController,
    viewModel: RecipeViewModel = koinViewModel()
) {
    val state = viewModel.state.value

    RecipeScreenContent(
        onAddRecipe = {
            navController.navigate(Screen.AddEditRecipeScreen.route)
        },
        onLoginClick = {
            navController.navigate(
                Screen.LoginScreen.route
            )
        },
        onSortClick = {
            viewModel.onEvent(RecipesEvent.ToggleOrderSection)
        },
        onEvent = {
            viewModel.onEvent(it)
        },
        onRecipeClicked = { recipe ->
            navController.navigate(
                Screen.AddEditRecipeScreen.route + "?recipeId=${recipe.id}"
            )
        },
        recipeState = state
    )
}

@Composable
private fun RecipeScreenContent(
    onAddRecipe: () -> Unit,
    onSortClick: () -> Unit,
    onLoginClick: () -> Unit,
    onEvent: (RecipesEvent) -> Unit,
    onRecipeClicked: (Recipe) -> Unit,
    recipeState: RecipesState
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddRecipe,
                modifier = Modifier.semantics { contentDescription = "New recipe" }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.add_recipe)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.your_recipes),
                    style = MaterialTheme.typography.bodyMedium
                )
                IconButton(
                    onClick = onSortClick
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = stringResource(id = R.string.sort)
                    )
                }
                IconButton(
                    onClick = onLoginClick
                ) {
                    when (recipeState.syncState) {
                        LoginState.LoginEmpty ->
                            Icon(
                                imageVector = Icons.Default.SyncProblem,
                                contentDescription = stringResource(id = R.string.sync_disabled)
                            )

                        LoginState.LoginPending ->
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = stringResource(id = R.string.sync_loading)
                            )

                        LoginState.LoginSuccess ->
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = stringResource(id = R.string.sync_enabled)
                            )

                        is LoginState.LoginFailure ->
                            Icon(
                                imageVector = Icons.Default.SyncProblem,
                                contentDescription = stringResource(id = R.string.sync_disabled)
                            )
                    }
                }
            }
            AnimatedVisibility(
                visible = recipeState.isOrderSelectionVisible,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                OrderSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    recipeOrder = recipeState.recipeOrder,
                    onOrderChange = {
                        onEvent(RecipesEvent.Order(it))
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyRecipesList(innerPadding, recipeState.recipes, onRecipeClicked)
        }
    }
}

@Composable
fun LazyRecipesList(
    innerPadding: PaddingValues,
    recipes: ImmutableRecipesList,
    onRecipeClicked: (Recipe) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        contentPadding = innerPadding
    ) {
        items(items = recipes.items, key = { it.title }) { recipe ->
            ListItem(
                headlineContent = {
                    Text(recipe.title)
                },
                supportingContent = {
                    Text(recipe.content.lines()
                        .take(2)
                        .joinToString("\n"))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onRecipeClicked(recipe)
                    }
            )
            HorizontalDivider()
        }
    }
}

class RecipeScreenStatePreviewParameterProvider : PreviewParameterProvider<RecipesState> {

    override val values = sequenceOf(
        RecipesState(
            ImmutableRecipesList(
                listOf(
                    Recipe(
                        "Recipe title",
                        content = "This is a long\nmultipline\ntext\nwith\nmany\nlines\nreally",
                        timestamp = 5
                    )
                )
            )
        ),
        RecipesState(),
        RecipesState(
            recipes = ImmutableRecipesList(List(10) { index ->
                Recipe(
                    "Recipe $index",
                    content = "Lorem ipsum dolor sit amet $index",
                    timestamp = 5
                )
            }
            )
        ),
        RecipesState(syncState = LoginState.LoginEmpty),
        RecipesState(syncState = LoginState.LoginPending),
        RecipesState(syncState = LoginState.LoginSuccess),
        RecipesState(syncState = LoginState.LoginFailure(error = NetworkError.UNAUTHORIZED)),
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RecipeScreenPreviewRealistic(
    @PreviewParameter(RecipeScreenStatePreviewParameterProvider::class) recipesState: RecipesState
) {
    MyApplicationTheme {
        RecipeScreenContent(
            onAddRecipe = {},
            onLoginClick = {},
            onSortClick = {},
            onEvent = {},
            onRecipeClicked = {},
            recipeState = recipesState
        )
    }
}
