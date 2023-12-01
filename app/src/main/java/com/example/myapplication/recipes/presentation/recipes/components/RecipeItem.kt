package com.example.myapplication.recipes.presentation.recipes.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.recipes.core.util.TestTags
import com.example.myapplication.recipes.domain.model.Recipe

@Composable
fun RecipeItem(recipe: Recipe, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.padding(16.dp)
            .testTag(TestTags.RECIPE_ITEM),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = modifier.padding(16.dp)
        ) {
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = recipe.content,
                style = MaterialTheme.typography.bodySmall,
                modifier = modifier.padding(top = 8.dp)
            )
        }
    }
}

@Preview
@Composable
fun RecipeCardPreview() {
    val recipe = Recipe(
        title = "Delicious Chocolate Cake",
        content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor " +
            "incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud" +
            "exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
        color = 3,
        timestamp = 1
    )
    RecipeItem(recipe)
}
