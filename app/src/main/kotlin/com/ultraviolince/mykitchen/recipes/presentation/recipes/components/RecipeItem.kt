package com.ultraviolince.mykitchen.recipes.presentation.recipes.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ultraviolince.mykitchen.R
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.model.SyncStatus

@Composable
fun RecipeItem(recipe: Recipe, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.padding(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = recipe.content,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            RecipeSyncStatusIcon(
                syncStatus = recipe.syncStatus,
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterVertically)
            )
        }
    }
}

@Composable
fun RecipeSyncStatusIcon(
    syncStatus: SyncStatus,
    modifier: Modifier = Modifier
) {
    when (syncStatus) {
        SyncStatus.NOT_SYNCED ->
            Icon(
                imageVector = Icons.Default.CloudQueue,
                contentDescription = stringResource(id = R.string.recipe_not_synced),
                modifier = modifier,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

        SyncStatus.SYNCING ->
            Icon(
                imageVector = Icons.Default.CloudSync,
                contentDescription = stringResource(id = R.string.recipe_syncing),
                modifier = modifier,
                tint = MaterialTheme.colorScheme.primary
            )

        SyncStatus.SYNCED ->
            Icon(
                imageVector = Icons.Default.CloudDone,
                contentDescription = stringResource(id = R.string.recipe_synced),
                modifier = modifier,
                tint = MaterialTheme.colorScheme.primary
            )

        SyncStatus.SYNC_ERROR ->
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = stringResource(id = R.string.recipe_sync_error),
                modifier = modifier,
                tint = MaterialTheme.colorScheme.error
            )
    }
}

@Preview
@Composable
internal fun RecipeCardPreview() {
    val recipe = Recipe(
        title = "Delicious Chocolate Cake",
        content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor " +
            "incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud" +
            "exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
        timestamp = 1,
        syncStatus = SyncStatus.SYNCED
    )
    RecipeItem(recipe)
}

@Preview
@Composable
internal fun RecipeCardNotSyncedPreview() {
    val recipe = Recipe(
        title = "Recipe Not Synced",
        content = "This recipe hasn't been synced to the server yet.",
        timestamp = 1,
        syncStatus = SyncStatus.NOT_SYNCED
    )
    RecipeItem(recipe)
}

@Preview
@Composable
internal fun RecipeCardSyncErrorPreview() {
    val recipe = Recipe(
        title = "Recipe Sync Error",
        content = "This recipe failed to sync to the server.",
        timestamp = 1,
        syncStatus = SyncStatus.SYNC_ERROR
    )
    RecipeItem(recipe)
}
