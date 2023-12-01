package com.example.myapplication.recipes.presentation.recipes

import android.annotation.SuppressLint
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.myapplication.recipes.core.util.TestTags
import com.example.myapplication.recipes.domain.model.Recipe
import com.example.myapplication.recipes.presentation.recipes.components.OrderSection
import com.example.myapplication.recipes.presentation.recipes.components.RecipeItem
import com.example.myapplication.recipes.presentation.util.Screen
import com.example.myapplication.ui.theme.MyApplicationTheme

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RecipeScreen(
    navController: NavController,
    viewModel: RecipeViewModel = hiltViewModel()
) {
    val state = viewModel.state.value

    RecipeScreenContent(
        onAddRecipe = {
            navController.navigate(Screen.AddEditRecipeScreen.route)
        },
        onSortClick = {
            viewModel.onEvent(RecipesEvent.ToggleOrderSection)
        },
        onEvent = {
            viewModel.onEvent(it)
        },
        recipeState = state
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
private fun RecipeScreenContent(
    onAddRecipe: () -> Unit,
    onSortClick: () -> Unit,
    onEvent: (RecipesEvent) -> Unit,
    recipeState: RecipesState
) {
    // val scaffoldState = rememberScrollState()
    // val scope = rememberCoroutineScope()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddRecipe) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add recipe")
            }
        }
    ) {
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
                Text(text = "Your recipe", style = MaterialTheme.typography.bodyMedium)
                IconButton(
                    onClick = onSortClick
                ) {
                    Icon(
                        imageVector = Icons.Default.Search, // TODO fix icon to sort
                        contentDescription = "Sort"
                    )
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
                        .padding(vertical = 16.dp)
                        .testTag(TestTags.ORDER_SECTION),
                    recipeOrder = recipeState.recipeOrder,
                    onOrderChange = {
                        onEvent(RecipesEvent.Order(it))
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn() {
                items(recipeState.recipes) { recipe ->
                    RecipeItem(
                        recipe = recipe,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                            } // TODO onDelete
                    )
                }
            }
        }
    }
}

class RecipeScreenStatePreviewParameterProvider : PreviewParameterProvider<RecipesState> {
    override val values = sequenceOf(
        RecipesState(),
        RecipesState(
            recipes = List(10) { index ->
                Recipe(
                    "Recipe $index",
                    content = "Lorem ipsum dolor sit amet $index",
                    timestamp = 5,
                    color = 5
                )
            }
        )
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RecipeScreenPreviewRealistic(
    @PreviewParameter(RecipeScreenStatePreviewParameterProvider::class) recipesState: RecipesState
) {
    MyApplicationTheme {
        RecipeScreenContent(
            onAddRecipe = {},
            onSortClick = {},
            onEvent = {},
            recipeState = recipesState
        )
    }
}
