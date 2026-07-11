package com.ultraviolince.mykitchen.ui.screens.addedit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.ultraviolince.mykitchen.domain.model.RecipeEnrichment
import com.ultraviolince.mykitchen.domain.model.RecipeLink
import com.ultraviolince.mykitchen.ui.theme.AppTheme
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.setResourceReaderAndroidContext

@OptIn(ExperimentalResourceApi::class)
@Preview(showBackground = true)
@Composable
internal fun AddEditScreenNewPreview() {
    val ctx = LocalContext.current
    remember(ctx) { setResourceReaderAndroidContext(ctx) }
    AppTheme {
        AddEditScreenContent(
            state = AddEditState(),
            recipeId = null,
            onNavigateBack = {},
            onTitleChange = {},
            onContentChange = {},
            onSave = {},
            onToggleBeautified = {},
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
@Preview(showBackground = true, name = "Edit Recipe")
@Composable
internal fun AddEditScreenEditPreview() {
    val ctx = LocalContext.current
    remember(ctx) { setResourceReaderAndroidContext(ctx) }
    AppTheme {
        AddEditScreenContent(
            state = AddEditState(title = "Pasta Carbonara", content = "1. Boil pasta\n2. Mix eggs and cheese\n3. Combine"),
            recipeId = "123",
            onNavigateBack = {},
            onTitleChange = {},
            onContentChange = {},
            onSave = {},
            onToggleBeautified = {},
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
@Preview(showBackground = true, name = "Beautified Recipe")
@Composable
internal fun AddEditScreenBeautifiedPreview() {
    val ctx = LocalContext.current
    remember(ctx) { setResourceReaderAndroidContext(ctx) }
    AppTheme {
        AddEditScreenContent(
            state = AddEditState(
                title = "Pasta Carbonara",
                content = "1. Boil pasta\n2. Mix eggs and cheese\n3. Combine",
                enrichment = RecipeEnrichment(
                    id = "enr-1",
                    recipeId = "123",
                    imageUrl = null,
                    imageCredit = null,
                    tags = listOf("quick", "budget-friendly"),
                    links = listOf(
                        RecipeLink(
                            title = "Classic Carbonara",
                            url = "https://example.com/carbonara",
                            description = "A traditional Roman recipe",
                        ),
                    ),
                    summary = "A creamy Roman classic made with eggs, cheese and pancetta.",
                    updatedAt = 0L,
                ),
                showBeautified = true,
            ),
            recipeId = "123",
            onNavigateBack = {},
            onTitleChange = {},
            onContentChange = {},
            onSave = {},
            onToggleBeautified = {},
        )
    }
}
