package com.ultraviolince.mykitchen.recipes.presentation.editrecipe

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat.ID_NULL
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.ultraviolince.mykitchen.R
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
    val imagePath = viewModel.recipeImagePath.value

    val snackBarHostState = remember { SnackbarHostState() }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.onEvent(AddEditRecipeEvent.ImageSelected(it.toString()))
        }
    }

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
        imagePath = imagePath,
        snackBarHostState = snackBarHostState,
        onImagePick = { imagePickerLauncher.launch("image/*") },
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
    imagePath: String?,
    snackBarHostState: SnackbarHostState,
    onImagePick: () -> Unit,
    modifier: Modifier = Modifier,
    eventHandler: (AddEditRecipeEvent) -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        floatingActionButton = {
            Row {
                FloatingActionButton(
                    onClick = onImagePick,
                    modifier = Modifier.semantics { contentDescription = "Add image" }
                ) {
                    Icon(imageVector = Icons.Default.Image, contentDescription = stringResource(id = R.string.add_image))
                }
                Spacer(modifier = Modifier.width(20.dp))
                RecipeActionButtons(
                    onSaveClick = { eventHandler(AddEditRecipeEvent.SaveRecipe) },
                    onDeleteClick = { eventHandler(AddEditRecipeEvent.DeleteRecipe) }
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            RecipeFormFields(
                titleState = titleState,
                contentState = contentState,
                onEvent = eventHandler,
                modifier = Modifier.fillMaxWidth()
            )

            // Image display section
            imagePath?.let { path ->
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = path,
                        contentDescription = "Recipe image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

data class AddEditRecipeState(
    val title: RecipeTextFieldState,
    val content: RecipeTextFieldState,
    val imagePath: String?
)

class AddEditRecipeScreenPreviewParameterProvider : PreviewParameterProvider<AddEditRecipeState> {
    override val values = sequenceOf(
        AddEditRecipeState(
            title = RecipeTextFieldState(text = "", hintStringId = R.string.title_hint, isHintVisible = true),
            content = RecipeTextFieldState(text = "", hintStringId = R.string.content_hint, isHintVisible = true),
            imagePath = null
        ),
        AddEditRecipeState(
            title = RecipeTextFieldState(text = "My Recipe", hintStringId = ID_NULL, isHintVisible = false),
            content = RecipeTextFieldState(text = "It goes like this...", hintStringId = ID_NULL, isHintVisible = false),
            imagePath = null
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
            imagePath = state.imagePath,
            snackBarHostState = SnackbarHostState(),
            onImagePick = {},
            eventHandler = {}
        )
    }
}
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
            imagePath = state.imagePath,
            snackBarHostState = SnackbarHostState(),
            onImagePick = {},
            eventHandler = {}
        )
    }
}
