package com.ultraviolince.mykitchen.recipes.presentation.recipes.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe

@Composable
fun RecipeItem(recipe: Recipe, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.padding(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
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
    }
}

@Preview
@Composable
private fun RecipeCardPreview() {
    val recipe = Recipe(
        title = "Delicious Chocolate Cake",
        content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor " +
            "incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud" +
            "exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
        timestamp = 1
    )
    RecipeItem(recipe)
}
