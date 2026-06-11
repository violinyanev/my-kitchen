package com.ultraviolince.mykitchen.recipes.presentation.editrecipe.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat.ID_NULL
import com.ultraviolince.mykitchen.R
import com.ultraviolince.mykitchen.recipes.presentation.common.components.AppTextField
import com.ultraviolince.mykitchen.recipes.presentation.editrecipe.AddEditRecipeEvent
import com.ultraviolince.mykitchen.recipes.presentation.editrecipe.RecipeTextFieldState
import com.ultraviolince.mykitchen.ui.theme.MyApplicationTheme

@Composable
fun RecipeFormFields(
    titleState: RecipeTextFieldState,
    contentState: RecipeTextFieldState,
    onEvent: (AddEditRecipeEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(16.dp))

        AppTextField(
            state = titleState,
            onValueChange = { onEvent(AddEditRecipeEvent.EnteredTitle(it)) },
            onFocusChanged = { onEvent(AddEditRecipeEvent.ChangeTitleFocus(it)) },
            contentDescriptionText = "Enter recipe title",
            textStyle = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        AppTextField(
            state = contentState,
            onValueChange = { onEvent(AddEditRecipeEvent.EnteredContent(it)) },
            onFocusChanged = { onEvent(AddEditRecipeEvent.ChangeContentFocus(it)) },
            contentDescriptionText = "Enter recipe content",
            textStyle = MaterialTheme.typography.bodyMedium,
            singleLine = false,
            minLines = 15
        )
    }
}

data class RecipeFormState(
    val title: RecipeTextFieldState,
    val content: RecipeTextFieldState
)

class RecipeFormFieldsPreviewParameterProvider : PreviewParameterProvider<RecipeFormState> {
    override val values = sequenceOf(
        RecipeFormState(
            title = RecipeTextFieldState(text = "", hintStringId = R.string.title_hint, isHintVisible = true),
            content = RecipeTextFieldState(text = "", hintStringId = R.string.content_hint, isHintVisible = true)
        ),
        RecipeFormState(
            title = RecipeTextFieldState(text = "Delicious Pasta", hintStringId = ID_NULL, isHintVisible = false),
            content = RecipeTextFieldState(text = "1. Boil water\n2. Add pasta\n3. Cook for 10 minutes\n4. Drain and serve", hintStringId = ID_NULL, isHintVisible = false)
        )
    )
}

@Preview(showBackground = true)
@Composable
internal fun RecipeFormFieldsPreview(
    @PreviewParameter(RecipeFormFieldsPreviewParameterProvider::class) state: RecipeFormState
) {
    MyApplicationTheme {
        RecipeFormFields(
            titleState = state.title,
            contentState = state.content,
            onEvent = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
