package com.ultraviolince.mykitchen.recipes.presentation.recipes.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.ultraviolince.mykitchen.R
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.util.NetworkError
import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
import com.ultraviolince.mykitchen.ui.theme.MyApplicationTheme

@Composable
fun SyncStatusIcon(
    syncState: LoginState,
    modifier: Modifier = Modifier
) {
    when (syncState) {
        LoginState.LoginEmpty ->
            Icon(
                imageVector = Icons.Default.SyncProblem,
                contentDescription = stringResource(id = R.string.sync_disabled),
                modifier = modifier
            )

        LoginState.LoginPending ->
            Icon(
                imageVector = Icons.Default.Sync,
                contentDescription = stringResource(id = R.string.sync_loading),
                modifier = modifier
            )

        LoginState.LoginSuccess ->
            Icon(
                imageVector = Icons.Default.CloudSync,
                contentDescription = stringResource(id = R.string.sync_enabled),
                modifier = modifier
            )

        is LoginState.LoginFailure ->
            Icon(
                imageVector = Icons.Default.SyncProblem,
                contentDescription = stringResource(id = R.string.sync_disabled),
                modifier = modifier
            )
    }
}

class SyncStatusIconPreviewParameterProvider : PreviewParameterProvider<LoginState> {
    override val values = sequenceOf(
        LoginState.LoginEmpty,
        LoginState.LoginPending,
        LoginState.LoginSuccess,
        LoginState.LoginFailure(error = NetworkError.UNAUTHORIZED)
    )
}

@Preview(showBackground = true)
@Composable
internal fun SyncStatusIconPreview(
    @PreviewParameter(SyncStatusIconPreviewParameterProvider::class) syncState: LoginState
) {
    MyApplicationTheme {
        SyncStatusIcon(syncState = syncState)
    }
}
