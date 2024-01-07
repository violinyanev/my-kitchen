package com.example.myapplication.recipes.presentation.editrecipe

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.myapplication.recipes.core.util.TestTags
import com.example.myapplication.recipes.presentation.editrecipe.components.TransparentHintTextField
import com.example.myapplication.ui.theme.MyApplicationTheme
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

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is AddEditRecipeViewModel.UiEvent.ShowSnackbar -> {
                    snackBarHostState.showSnackbar(
                        message = event.message
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
    })
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AddEditRecipeScreenContent(
    titleState: RecipeTextFieldState,
    contentState: RecipeTextFieldState,
    snackBarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    eventHandler: (AddEditRecipeEvent) -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    eventHandler(AddEditRecipeEvent.SaveRecipe)
                }
                // backgroundColor = MaterialTheme.colors.primary
            ) {
                Icon(imageVector = Icons.Default.Done, contentDescription = "Save")
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
            TransparentHintTextField(
                text = titleState.text,
                hint = titleState.hint,
                onValueChange = {
                    eventHandler(AddEditRecipeEvent.EnteredTitle(it))
                },
                onFocusChange = {
                    eventHandler(AddEditRecipeEvent.ChangeTitleFocus(it))
                },
                isHintVisible = titleState.isHintVisible,
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineMedium,
                testTag = TestTags.TITLE_TEXT_FIELD
            )
            Spacer(modifier = Modifier.height(16.dp))
            TransparentHintTextField(
                text = contentState.text,
                hint = contentState.hint,
                onValueChange = {
                    eventHandler(AddEditRecipeEvent.EnteredContent(it))
                },
                onFocusChange = {
                    eventHandler(AddEditRecipeEvent.ChangeContentFocus(it))
                },
                isHintVisible = contentState.isHintVisible,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxHeight(),
                testTag = TestTags.CONTENT_TEXT_FIELD
            )
        }
    }
}

data class AddEditRecipeState(
    val title: RecipeTextFieldState,
    val content: RecipeTextFieldState,
)

class AddEditRecipeScreenPreviewParameterProvider : PreviewParameterProvider<AddEditRecipeState> {
    override val values = sequenceOf(
        AddEditRecipeState(
            title = RecipeTextFieldState(text = "", hint = "enter title...", isHintVisible = true),
            content = RecipeTextFieldState(text = "", hint = "enter content...", isHintVisible = true),
        ),
        AddEditRecipeState(
            title = RecipeTextFieldState(text = "My Recipe", hint = "", isHintVisible = false),
            content = RecipeTextFieldState(text = "It goes like this...", hint = "", isHintVisible = false),
        ),
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
            eventHandler = {},
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
            eventHandler = {},
        )
    }
}
