package com.ultraviolince.mykitchen.recipes.presentation.recipes.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.ultraviolince.mykitchen.R
import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
import com.ultraviolince.mykitchen.ui.theme.MyApplicationTheme

@Composable
fun RecipeListHeader(
    syncState: LoginState,
    onSortClick: () -> Unit,
    onSyncClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onSortClick) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = stringResource(id = R.string.sort)
            )
        }
        IconButton(onClick = onSyncClick) {
            SyncStatusIcon(syncState = syncState)
        }
    }
}

class RecipeListHeaderPreviewParameterProvider : PreviewParameterProvider<LoginState> {
    override val values = sequenceOf(
        LoginState.LoginEmpty,
        LoginState.LoginPending,
        LoginState.LoginSuccess
    )
}

@Preview(showBackground = true)
@Composable
internal fun RecipeListHeaderPreview(
    @PreviewParameter(RecipeListHeaderPreviewParameterProvider::class) syncState: LoginState
) {
    MyApplicationTheme {
        RecipeListHeader(
            syncState = syncState,
            onSortClick = {},
            onSyncClick = {}
        )
    }
}
