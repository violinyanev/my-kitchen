package com.ultraviolince.mykitchen.recipes.presentation.editrecipe.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ultraviolince.mykitchen.R
import com.ultraviolince.mykitchen.ui.theme.MyApplicationTheme

@Composable
fun RecipeActionButtons(
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        FloatingActionButton(
            onClick = onDeleteClick,
            modifier = Modifier.semantics { 
                contentDescription = "Delete recipe"
                customActions = listOf(
                    CustomAccessibilityAction(
                        label = "Delete this recipe",
                        action = {
                            onDeleteClick()
                            true
                        }
                    )
                )
            }
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(id = R.string.delete)
            )
        }

        Spacer(modifier = Modifier.width(20.dp))

        FloatingActionButton(
            onClick = onSaveClick,
            modifier = Modifier.semantics { 
                contentDescription = "Save recipe"
                customActions = listOf(
                    CustomAccessibilityAction(
                        label = "Save this recipe",
                        action = {
                            onSaveClick()
                            true
                        }
                    )
                )
            }
        ) {
            Icon(
                imageVector = Icons.Default.Done,
                contentDescription = stringResource(id = R.string.save)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
internal fun RecipeActionButtonsPreview() {
    MyApplicationTheme {
        RecipeActionButtons(
            onSaveClick = {},
            onDeleteClick = {}
        )
    }
}
