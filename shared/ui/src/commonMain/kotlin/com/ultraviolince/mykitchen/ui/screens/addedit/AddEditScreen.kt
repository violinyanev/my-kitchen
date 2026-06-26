package com.ultraviolince.mykitchen.ui.screens.addedit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ultraviolince.mykitchen.ui.generated.resources.Res
import com.ultraviolince.mykitchen.ui.generated.resources.back
import com.ultraviolince.mykitchen.ui.generated.resources.content_hint
import com.ultraviolince.mykitchen.ui.generated.resources.edit_recipe
import com.ultraviolince.mykitchen.ui.generated.resources.new_recipe
import com.ultraviolince.mykitchen.ui.generated.resources.save
import com.ultraviolince.mykitchen.ui.generated.resources.title_hint
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    recipeId: String?,
    onNavigateBack: () -> Unit,
    viewModel: AddEditViewModel = koinViewModel(parameters = { parametersOf(recipeId) }),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onNavigateBack()
    }

    AddEditScreenContent(
        state = state,
        recipeId = recipeId,
        onNavigateBack = onNavigateBack,
        onTitleChange = viewModel::onTitleChange,
        onContentChange = viewModel::onContentChange,
        onSave = viewModel::save,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddEditScreenContent(
    state: AddEditState,
    recipeId: String?,
    onNavigateBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (recipeId == null) stringResource(Res.string.new_recipe) else stringResource(Res.string.edit_recipe)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back))
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .imePadding(),
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = onTitleChange,
                label = { Text(stringResource(Res.string.title_hint)) },
                modifier = Modifier.fillMaxWidth().testTag("title_field"),
                isError = state.titleError != null,
                supportingText = state.titleError?.let { error -> { Text(stringResource(error)) } },
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = state.content,
                onValueChange = onContentChange,
                label = { Text(stringResource(Res.string.content_hint)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("content_field"),
                minLines = 5,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth().testTag("save_button"),
                enabled = !state.isLoading,
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator()
                } else {
                    Text(stringResource(Res.string.save))
                }
            }
        }
    }
}
