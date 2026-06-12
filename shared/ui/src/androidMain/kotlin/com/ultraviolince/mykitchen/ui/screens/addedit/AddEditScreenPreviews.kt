package com.ultraviolince.mykitchen.ui.screens.addedit

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.ultraviolince.mykitchen.ui.theme.AppTheme

@Preview(showBackground = true)
@Composable
internal fun AddEditScreenNewPreview() {
    AppTheme {
        AddEditScreenContent(
            state = AddEditState(),
            recipeId = null,
            onNavigateBack = {},
            onTitleChange = {},
            onContentChange = {},
            onSave = {},
        )
    }
}

@Preview(showBackground = true, name = "Edit Recipe")
@Composable
internal fun AddEditScreenEditPreview() {
    AppTheme {
        AddEditScreenContent(
            state = AddEditState(title = "Pasta Carbonara", content = "1. Boil pasta\n2. Mix eggs and cheese\n3. Combine"),
            recipeId = "123",
            onNavigateBack = {},
            onTitleChange = {},
            onContentChange = {},
            onSave = {},
        )
    }
}
