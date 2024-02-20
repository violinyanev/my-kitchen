package com.ultraviolince.mykitchen.recipes.presentation.editrecipe

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat.ID_NULL
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ultraviolince.mykitchen.R
import com.ultraviolince.mykitchen.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AddEditRecipeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: AddEditRecipeViewModel = hiltViewModel()
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

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
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
            FloatingActionButton(
                onClick = {
                    eventHandler(AddEditRecipeEvent.SaveRecipe)
                },
                modifier = Modifier.semantics { contentDescription = "Save recipe" }
            ) {
                Icon(imageVector = Icons.Default.Done, contentDescription = stringResource(id = R.string.save))
            }
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Box {
                TextField(
                    value = titleState.text,
                    onValueChange = {
                        eventHandler(AddEditRecipeEvent.EnteredTitle(it))
                    },
                    placeholder = {
                        if (titleState.hintStringId != ID_NULL) {
                            Text(text = stringResource(titleState.hintStringId), style = MaterialTheme.typography.headlineMedium)
                        }
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged {
                            eventHandler(AddEditRecipeEvent.ChangeTitleFocus(it))
                        }
                        .semantics { contentDescription = "Enter recipe title" }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box {
                TextField(
                    value = contentState.text,
                    onValueChange = {
                        eventHandler(AddEditRecipeEvent.EnteredContent(it))
                    },
                    minLines = 15,
                    placeholder = {
                        if (titleState.hintStringId != ID_NULL) {
                            Text(
                                text = stringResource(contentState.hintStringId),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    },
                    singleLine = false,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged {
                            eventHandler(AddEditRecipeEvent.ChangeTitleFocus(it))
                        }
                        .semantics { contentDescription = "Enter recipe content" }
                )
            }
        }
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

@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_UNDEFINED)
@Composable
private fun AddEditRecipeScreenPreview(
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

@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AddEditRecipeScreenPreviewNightMode(
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
