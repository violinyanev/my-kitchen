package recipes.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import domain.model.Recipe

@Composable
fun RecipeItem(recipe: Recipe, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.padding(16.dp),
        color = MaterialTheme.colors.surface
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.body1
            )
            Text(
                text = recipe.content,
                style = MaterialTheme.typography.body2,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

// @Preview
// @Composable
// private fun RecipeCardPreview() {
//    val recipe = Recipe(
//        title = "Delicious Chocolate Cake",
//        content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor " +
//            "incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud" +
//            "exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
//        timestamp = 1
//    )
//    RecipeItem(recipe)
// }
