package editrecipe.presentation

import AddEditRecipeViewModel
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collectLatest
import mykitchen.composeapp.generated.resources.Res
import mykitchen.composeapp.generated.resources.delete
import mykitchen.composeapp.generated.resources.empty
import mykitchen.composeapp.generated.resources.save
import org.jetbrains.compose.resources.stringResource
import shared.state.TextFieldState

@Composable
fun AddEditRecipeScreen(
    navController: NavController,
    viewModel: AddEditRecipeViewModel, // TODO kmp = koinViewModel()
    modifier: Modifier = Modifier
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
    titleState: TextFieldState,
    contentState: TextFieldState,
    snackBarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    eventHandler: (AddEditRecipeEvent) -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        floatingActionButton = {
            Row {
                FloatingActionButton(
                    onClick = {
                        eventHandler(AddEditRecipeEvent.DeleteRecipe)
                    },
                    // TODO can we reuse the string from below?
                    modifier = Modifier.semantics { contentDescription = "Delete recipe" }
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(resource = Res.string.delete))
                }
                Spacer(modifier = Modifier.width(20.dp))
                FloatingActionButton(
                    onClick = {
                        eventHandler(AddEditRecipeEvent.SaveRecipe)
                    },
                    // TODO can we reuse the string from below?
                    modifier = Modifier.semantics { contentDescription = "Save recipe" }
                ) {
                    Icon(imageVector = Icons.Default.Done, contentDescription = stringResource(resource = Res.string.save))
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Box {
                TextField(
                    value = titleState.text,
                    onValueChange = {
                        eventHandler(AddEditRecipeEvent.EnteredTitle(it))
                    },
                    placeholder = {
                        if (titleState.hintStringId != Res.string.empty) {
                            Text(text = stringResource(titleState.hintStringId), style = MaterialTheme.typography.h4)
                        }
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.h4,
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
                        if (titleState.hintStringId != Res.string.empty) {
                            Text(
                                text = stringResource(contentState.hintStringId),
                                style = MaterialTheme.typography.body1
                            )
                        }
                    },
                    singleLine = false,
                    textStyle = MaterialTheme.typography.body2,
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

// data class AddEditRecipeState(
//    val title: TextFieldState,
//    val content: TextFieldState
// )
//
// class AddEditRecipeScreenPreviewParameterProvider : PreviewParameterProvider<AddEditRecipeState> {
//    override val values = sequenceOf(
//        AddEditRecipeState(
//            title = TextFieldState(text = "", hintStringId = R.string.title_hint, isHintVisible = true),
//            content = TextFieldState(text = "", hintStringId = R.string.content_hint, isHintVisible = true)
//        ),
//        AddEditRecipeState(
//            title = TextFieldState(text = "My Recipe", hintStringId = ID_NULL, isHintVisible = false),
//            content = TextFieldState(text = "It goes like this...", hintStringId = ID_NULL, isHintVisible = false)
//        )
//    )
// }
//
// @Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_UNDEFINED)
// @Composable
// private fun AddEditRecipeScreenPreview(
//    @PreviewParameter(AddEditRecipeScreenPreviewParameterProvider::class) state: AddEditRecipeState
// ) {
//    MyApplicationTheme {
//        AddEditRecipeScreenContent(
//            titleState = state.title,
//            contentState = state.content,
//            snackBarHostState = SnackbarHostState(),
//            eventHandler = {}
//        )
//    }
// }
//
// @Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
// @Composable
// private fun AddEditRecipeScreenPreviewNightMode(
//    @PreviewParameter(AddEditRecipeScreenPreviewParameterProvider::class) state: AddEditRecipeState
// ) {
//    MyApplicationTheme {
//        AddEditRecipeScreenContent(
//            titleState = state.title,
//            contentState = state.content,
//            snackBarHostState = SnackbarHostState(),
//            eventHandler = {}
//        )
//    }
// }
