package com.ultraviolince.mykitchen.ui.screens.recipelist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ultraviolince.mykitchen.domain.model.AuthState
import com.ultraviolince.mykitchen.domain.model.Recipe
import com.ultraviolince.mykitchen.domain.model.RecipeOrder
import com.ultraviolince.mykitchen.ui.generated.resources.Res
import com.ultraviolince.mykitchen.ui.generated.resources.add_recipe
import com.ultraviolince.mykitchen.ui.generated.resources.beautify_recipe
import com.ultraviolince.mykitchen.ui.generated.resources.delete_recipe
import com.ultraviolince.mykitchen.ui.generated.resources.logout
import com.ultraviolince.mykitchen.ui.generated.resources.no_recipes
import com.ultraviolince.mykitchen.ui.generated.resources.sort_date
import com.ultraviolince.mykitchen.ui.generated.resources.sort_title
import org.jetbrains.compose.resources.getString
import com.ultraviolince.mykitchen.ui.generated.resources.sync
import com.ultraviolince.mykitchen.ui.generated.resources.your_recipes
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    onAddRecipe: () -> Unit,
    onEditRecipe: (String) -> Unit,
    onBeautify: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: RecipeListViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.LoggedOut -> onNavigateToLogin()
            is AuthState.LoggedIn -> viewModel.sync()
        }
    }

    RecipeListScreenContent(
        state = state,
        onAddRecipe = onAddRecipe,
        onEditRecipe = onEditRecipe,
        onBeautify = onBeautify,
        onSync = viewModel::sync,
        onLogout = viewModel::logout,
        onDelete = viewModel::delete,
        onOrderChange = viewModel::setOrder,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreenContent(
    state: RecipeListState,
    onAddRecipe: () -> Unit,
    onEditRecipe: (String) -> Unit,
    onBeautify: (String) -> Unit,
    onSync: () -> Unit,
    onLogout: () -> Unit,
    onDelete: (String) -> Unit,
    onOrderChange: (RecipeOrder) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(getString(it)) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.your_recipes)) },
                actions = {
                    if (state.isSyncing) {
                        CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                    } else {
                        IconButton(onClick = onSync) {
                            Icon(Icons.Default.Refresh, contentDescription = stringResource(Res.string.sync))
                        }
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = stringResource(Res.string.logout))
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddRecipe) {
                Icon(Icons.Default.Add, contentDescription = stringResource(Res.string.add_recipe))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            OrderToggle(
                currentOrder = state.order,
                onOrderChange = onOrderChange,
            )
            if (state.recipes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(Res.string.no_recipes), style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.recipes, key = { it.id }) { recipe ->
                        RecipeItem(
                            recipe = recipe,
                            onClick = { onEditRecipe(recipe.id) },
                            onDelete = { onDelete(recipe.id) },
                            onBeautify = { onBeautify(recipe.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderToggle(currentOrder: RecipeOrder, onOrderChange: (RecipeOrder) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.End,
    ) {
        TextButton(onClick = { onOrderChange(RecipeOrder.Title()) }) {
            Text(
                stringResource(Res.string.sort_title),
                color = if (currentOrder is RecipeOrder.Title) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        TextButton(onClick = { onOrderChange(RecipeOrder.Date()) }) {
            Text(
                stringResource(Res.string.sort_date),
                color = if (currentOrder is RecipeOrder.Date) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun RecipeItem(
    recipe: Recipe,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onBeautify: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(recipe.title, style = MaterialTheme.typography.titleMedium)
                if (recipe.content.isNotBlank()) {
                    Text(
                        recipe.content.take(80),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                    )
                }
            }
            IconButton(onClick = onBeautify) {
                Icon(Icons.Default.AutoAwesome, contentDescription = stringResource(Res.string.beautify_recipe))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(Res.string.delete_recipe))
            }
        }
    }
}
