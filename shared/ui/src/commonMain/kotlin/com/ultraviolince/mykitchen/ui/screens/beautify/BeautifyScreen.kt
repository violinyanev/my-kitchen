package com.ultraviolince.mykitchen.ui.screens.beautify

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ultraviolince.mykitchen.domain.model.RecipeEnrichment
import com.ultraviolince.mykitchen.domain.model.RecipeLink
import com.ultraviolince.mykitchen.ui.generated.resources.Res
import com.ultraviolince.mykitchen.ui.generated.resources.back
import com.ultraviolince.mykitchen.ui.generated.resources.beautify_accept
import com.ultraviolince.mykitchen.ui.generated.resources.beautify_disclaimer
import com.ultraviolince.mykitchen.ui.generated.resources.beautify_links
import com.ultraviolince.mykitchen.ui.generated.resources.beautify_loading
import com.ultraviolince.mykitchen.ui.generated.resources.beautify_photo_credit
import com.ultraviolince.mykitchen.ui.generated.resources.beautify_recipe
import com.ultraviolince.mykitchen.ui.generated.resources.beautify_refine
import com.ultraviolince.mykitchen.ui.generated.resources.beautify_refine_cancel
import com.ultraviolince.mykitchen.ui.generated.resources.beautify_refine_hint
import com.ultraviolince.mykitchen.ui.generated.resources.beautify_refine_send
import com.ultraviolince.mykitchen.ui.generated.resources.beautify_reject
import com.ultraviolince.mykitchen.ui.generated.resources.beautify_summary
import com.ultraviolince.mykitchen.ui.generated.resources.beautify_tags
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeautifyScreen(
    recipeId: String,
    onNavigateBack: () -> Unit,
    viewModel: BeautifyViewModel = koinViewModel(parameters = { parametersOf(recipeId) }),
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect((state as? BeautifyUiState.Reviewing)?.error) {
        val error = (state as? BeautifyUiState.Reviewing)?.error ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(getString(error))
        viewModel.clearError()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.beautify_recipe)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.back),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        when (val s = state) {
            is BeautifyUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            stringResource(Res.string.beautify_loading),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            is BeautifyUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            stringResource(s.message),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = viewModel::beautifyRecipe) {
                            Text("Retry")
                        }
                    }
                }
            }

            is BeautifyUiState.Reviewing -> {
                EnrichmentReviewContent(
                    enrichment = s.enrichment,
                    isRefining = s.isRefining,
                    onAccept = onNavigateBack,
                    onReject = { viewModel.rejectEnrichment(onNavigateBack) },
                    onRefine = viewModel::refineEnrichment,
                    modifier = Modifier.padding(paddingValues),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EnrichmentReviewContent(
    enrichment: RecipeEnrichment,
    isRefining: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onRefine: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showRefineInput by rememberSaveable { mutableStateOf(false) }
    var refineFeedback by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        enrichment.imageUrl?.let { url ->
            AsyncImage(
                model = url,
                contentDescription = enrichment.imageCredit,
                modifier = Modifier.fillMaxWidth().height(200.dp),
                contentScale = ContentScale.Crop,
            )
            enrichment.imageCredit?.let { credit ->
                Text(
                    stringResource(Res.string.beautify_photo_credit, credit),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (enrichment.summary.isNotBlank()) {
            Text(stringResource(Res.string.beautify_summary), style = MaterialTheme.typography.titleSmall)
            Text(enrichment.summary, style = MaterialTheme.typography.bodyMedium)
        }

        if (enrichment.tags.isNotEmpty()) {
            Text(stringResource(Res.string.beautify_tags), style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                enrichment.tags.forEach { tag ->
                    AssistChip(onClick = {}, label = { Text(tag) })
                }
            }
        }

        if (enrichment.links.isNotEmpty()) {
            Text(stringResource(Res.string.beautify_links), style = MaterialTheme.typography.titleSmall)
            enrichment.links.forEach { link ->
                RecipeLinkCard(link)
            }
            Text(
                stringResource(Res.string.beautify_disclaimer),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (showRefineInput) {
            OutlinedTextField(
                value = refineFeedback,
                onValueChange = { refineFeedback = it },
                label = { Text(stringResource(Res.string.beautify_refine_hint)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isRefining) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = {
                            if (refineFeedback.isNotBlank()) {
                                onRefine(refineFeedback)
                                refineFeedback = ""
                                showRefineInput = false
                            }
                        },
                    ) {
                        Text(stringResource(Res.string.beautify_refine_send))
                    }
                    OutlinedButton(onClick = {
                        showRefineInput = false
                        refineFeedback = ""
                    }) {
                        Text(stringResource(Res.string.beautify_refine_cancel))
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = onAccept,
                modifier = Modifier.weight(1f),
                enabled = !isRefining,
            ) {
                Text(stringResource(Res.string.beautify_accept))
            }
            OutlinedButton(
                onClick = onReject,
                modifier = Modifier.weight(1f),
                enabled = !isRefining,
            ) {
                Text(stringResource(Res.string.beautify_reject))
            }
        }

        if (!showRefineInput && !isRefining) {
            TextButton(
                onClick = { showRefineInput = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.beautify_refine))
            }
        }
    }
}

@Composable
private fun RecipeLinkCard(link: RecipeLink) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(link.title, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(link.description, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                link.url,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
