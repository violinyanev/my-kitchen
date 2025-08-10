package com.ultraviolince.mykitchen.recipes.presentation.common.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.res.ResourcesCompat.ID_NULL
import com.ultraviolince.mykitchen.R
import com.ultraviolince.mykitchen.recipes.presentation.editrecipe.RecipeTextFieldState
import com.ultraviolince.mykitchen.ui.theme.MyApplicationTheme

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    hintStringId: Int,
    textStyle: TextStyle,
    isHintVisible: Boolean,
    onFocusChanged: (FocusState) -> Unit,
    contentDescriptionText: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
    readOnly: Boolean = false
) {
    Box(modifier = modifier) {
        TextField(
            value = value,
            readOnly = readOnly,
            onValueChange = onValueChange,
            placeholder = {
                if (hintStringId != ID_NULL && isHintVisible) {
                    Text(
                        text = stringResource(hintStringId),
                        style = textStyle
                    )
                }
            },
            singleLine = singleLine,
            minLines = minLines,
            textStyle = textStyle,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged(onFocusChanged)
                .semantics { contentDescription = contentDescriptionText }
        )
    }
}

@Composable
fun AppTextField(
    state: RecipeTextFieldState,
    onValueChange: (String) -> Unit,
    onFocusChanged: (FocusState) -> Unit,
    contentDescriptionText: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    singleLine: Boolean = true,
    minLines: Int = 1,
    readOnly: Boolean = false
) {
    AppTextField(
        value = state.text,
        onValueChange = onValueChange,
        hintStringId = state.hintStringId,
        textStyle = textStyle,
        isHintVisible = state.isHintVisible,
        onFocusChanged = onFocusChanged,
        contentDescriptionText = contentDescriptionText,
        modifier = modifier,
        singleLine = singleLine,
        minLines = minLines,
        readOnly = readOnly
    )
}

@Preview(showBackground = true)
@Composable
private fun AppTextFieldPreview() {
    MyApplicationTheme {
        AppTextField(
            value = "",
            onValueChange = {},
            hintStringId = R.string.title_hint,
            textStyle = MaterialTheme.typography.bodyMedium,
            isHintVisible = true,
            onFocusChanged = {},
            contentDescriptionText = "Sample text field"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AppTextFieldWithValuePreview() {
    MyApplicationTheme {
        AppTextField(
            value = "Sample text content",
            onValueChange = {},
            hintStringId = R.string.title_hint,
            textStyle = MaterialTheme.typography.bodyMedium,
            isHintVisible = false,
            onFocusChanged = {},
            contentDescriptionText = "Sample text field"
        )
    }
}
