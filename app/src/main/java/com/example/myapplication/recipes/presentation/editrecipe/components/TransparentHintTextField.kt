package com.example.myapplication.recipes.presentation.editrecipe.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun TransparentHintTextField(
    text: String,
    hint: String,
    onFocusChange: (FocusState) -> Unit,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isHintVisible: Boolean = true,
    textStyle: TextStyle = TextStyle(),
    singleLine: Boolean = false,
    testTag: String = ""
) {
    Box(
        modifier = modifier
    ) {
        TextField(
            value = text,
            onValueChange = onValueChange,
            singleLine = singleLine,
            textStyle = textStyle,
            modifier = Modifier.fillMaxWidth()
                .onFocusChanged {
                    onFocusChange(it)
                }
                .testTag(testTag)
        )
        if (isHintVisible) {
            Text(text = hint, style = textStyle)
        }
    }
}

@Preview
@Composable
fun TransparentHintTextFieldPreview() {
    TransparentHintTextField(
        text = "Text",
        hint = "",
        onFocusChange = {},
        onValueChange = {}
    )
}

@Preview
@Composable
fun TransparentHintTextFieldHinyPreview() {
    TransparentHintTextField(
        text = "",
        hint = "Hint",
        onFocusChange = {},
        onValueChange = {}
    )
}
