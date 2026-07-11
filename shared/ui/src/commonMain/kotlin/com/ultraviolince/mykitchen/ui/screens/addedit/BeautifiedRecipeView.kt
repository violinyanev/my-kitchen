package com.ultraviolince.mykitchen.ui.screens.addedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ultraviolince.mykitchen.domain.model.RecipeEnrichment
import com.ultraviolince.mykitchen.domain.model.RecipeLink
import com.ultraviolince.mykitchen.ui.generated.resources.Res
import com.ultraviolince.mykitchen.ui.generated.resources.beautify_disclaimer
import com.ultraviolince.mykitchen.ui.generated.resources.beautify_links
import com.ultraviolince.mykitchen.ui.generated.resources.beautify_photo_credit
import com.ultraviolince.mykitchen.ui.generated.resources.beautify_summary
import com.ultraviolince.mykitchen.ui.generated.resources.beautify_tags
import org.jetbrains.compose.resources.stringResource

/**
 * Read-only "beautified" rendition of a recipe: the server-generated
 * enrichment (cover image, summary, tags, links) around the recipe itself.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun BeautifiedRecipeView(
    title: String,
    content: String,
    enrichment: RecipeEnrichment,
    modifier: Modifier = Modifier,
) {
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

        Text(title, style = MaterialTheme.typography.headlineSmall)

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

        if (content.isNotBlank()) {
            Text(content, style = MaterialTheme.typography.bodyMedium)
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
    }
}

@Composable
private fun RecipeLinkCard(link: RecipeLink) {
    val uriHandler = LocalUriHandler.current
    Card(
        onClick = { uriHandler.openUri(link.url) },
        modifier = Modifier.fillMaxWidth(),
    ) {
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
