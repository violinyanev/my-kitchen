package com.ultraviolince.mykitchen.recipes.presentation.common.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.password
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.res.ResourcesCompat.ID_NULL
import com.ultraviolince.mykitchen.shared.R
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
    readOnly: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null
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
            isError = isError,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged(onFocusChanged)
                .semantics {
                    contentDescription = buildString {
                        append(contentDescriptionText)
                        if (isError && errorMessage != null) {
                            append(". Error: $errorMessage")
                        }
                    }
                }
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
    readOnly: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null
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
        readOnly = readOnly,
        isError = isError,
        errorMessage = errorMessage
    )
}

@Composable
fun AppPasswordField(
    state: RecipeTextFieldState,
    onValueChange: (String) -> Unit,
    onFocusChanged: (FocusState) -> Unit,
    contentDescriptionText: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Box(modifier = modifier) {
        TextField(
            value = state.text,
            onValueChange = onValueChange,
            placeholder = {
                if (state.hintStringId != ID_NULL && state.isHintVisible) {
                    Text(
                        text = stringResource(state.hintStringId),
                        style = textStyle
                    )
                }
            },
            singleLine = true,
            textStyle = textStyle,
            isError = isError,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged(onFocusChanged)
                .semantics {
                    password()
                    contentDescription = buildString {
                        append(contentDescriptionText)
                        if (isError && errorMessage != null) {
                            append(". Error: $errorMessage")
                        }
                    }
                }
        )
    }
}

@Preview(showBackground = true)
@Composable
internal fun AppTextFieldWithValuePreview() {
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

@Preview(showBackground = true)
@Composable
internal fun AppPasswordFieldPreview() {
    MyApplicationTheme {
        AppPasswordField(
            state = RecipeTextFieldState(text = "", hintStringId = R.string.password_hint, isHintVisible = true),
            onValueChange = {},
            onFocusChanged = {},
            contentDescriptionText = "Password"
        )
    }
}
