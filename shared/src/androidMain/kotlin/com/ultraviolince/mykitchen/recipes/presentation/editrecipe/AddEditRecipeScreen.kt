package com.ultraviolince.mykitchen.recipes.presentation.editrecipe

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.core.content.res.ResourcesCompat.ID_NULL
import androidx.navigation.NavController
import com.ultraviolince.mykitchen.shared.R
import com.ultraviolince.mykitchen.recipes.presentation.editrecipe.components.RecipeActionButtons
import com.ultraviolince.mykitchen.recipes.presentation.editrecipe.components.RecipeFormFields
import com.ultraviolince.mykitchen.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@Composable
fun AddEditRecipeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: AddEditRecipeViewModel = koinViewModel()
) {
    val titleState = viewModel.recipeTitle.value
    val contentState = viewModel.recipeContent.value

    val snackBarHostState = remember { SnackbarHostState() }

    // TODO better way?
    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is AddEditRecipeViewModel.UiEvent.ShowSnackbar -> {
                    snackBarHostState.showSnackbar(
                        message = context.resources.getString(event.message)
                    )
                }
                is AddEditRecipeViewModel.UiEvent.SaveRecipe -> {
                    navController.navigateUp()
                }
                is AddEditRecipeViewModel.UiEvent.DeleteRecipe -> {
                    navController.navigateUp()
                }
            }
        }
    }

    AddEditRecipeScreenContent(
        titleState = titleState,
        contentState = contentState,
        snackBarHostState = snackBarHostState,
        eventHandler = {
            viewModel.onEvent(it)
        },
        modifier = modifier
    )
}

@Composable
fun AddEditRecipeScreenContent(
    titleState: RecipeTextFieldState,
    contentState: RecipeTextFieldState,
    snackBarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    eventHandler: (AddEditRecipeEvent) -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        floatingActionButton = {
            RecipeActionButtons(
                onSaveClick = { eventHandler(AddEditRecipeEvent.SaveRecipe) },
                onDeleteClick = { eventHandler(AddEditRecipeEvent.DeleteRecipe) }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        RecipeFormFields(
            titleState = titleState,
            contentState = contentState,
            onEvent = eventHandler,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

data class AddEditRecipeState(
    val title: RecipeTextFieldState,
    val content: RecipeTextFieldState
)

class AddEditRecipeScreenPreviewParameterProvider : PreviewParameterProvider<AddEditRecipeState> {
    override val values = sequenceOf(
        AddEditRecipeState(
            title = RecipeTextFieldState(text = "", hintStringId = R.string.title_hint, isHintVisible = true),
            content = RecipeTextFieldState(text = "", hintStringId = R.string.content_hint, isHintVisible = true)
        ),
        AddEditRecipeState(
            title = RecipeTextFieldState(text = "My Recipe", hintStringId = ID_NULL, isHintVisible = false),
            content = RecipeTextFieldState(text = "It goes like this...", hintStringId = ID_NULL, isHintVisible = false)
        )
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
internal fun AddEditRecipeScreenLightPreview(
    @PreviewParameter(AddEditRecipeScreenPreviewParameterProvider::class) state: AddEditRecipeState
) {
    MyApplicationTheme {
        AddEditRecipeScreenContent(
            titleState = state.title,
            contentState = state.content,
            snackBarHostState = SnackbarHostState(),
            eventHandler = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
internal fun AddEditRecipeScreenDarkPreview(
    @PreviewParameter(AddEditRecipeScreenPreviewParameterProvider::class) state: AddEditRecipeState
) {
    MyApplicationTheme {
        AddEditRecipeScreenContent(
            titleState = state.title,
            contentState = state.content,
            snackBarHostState = SnackbarHostState(),
            eventHandler = {}
        )
    }
}
