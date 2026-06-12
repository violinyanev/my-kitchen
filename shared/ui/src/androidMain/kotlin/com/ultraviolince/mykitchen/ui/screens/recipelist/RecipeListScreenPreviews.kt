package com.ultraviolince.mykitchen.ui.screens.recipelist

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.ultraviolince.mykitchen.domain.model.Recipe
import com.ultraviolince.mykitchen.domain.model.RecipeOrder
import com.ultraviolince.mykitchen.ui.theme.AppTheme

@Preview(showBackground = true)
@Composable
internal fun RecipeListScreenEmptyPreview() {
    AppTheme {
        RecipeListScreenContent(
            state = RecipeListState(),
            onAddRecipe = {},
            onEditRecipe = {},
            onSync = {},
            onLogout = {},
            onDelete = {},
            onOrderChange = {},
        )
    }
}

@Preview(showBackground = true, name = "Recipe List With Items")
@Composable
internal fun RecipeListScreenWithItemsPreview() {
    AppTheme {
        RecipeListScreenContent(
            state = RecipeListState(
                recipes = listOf(
                    Recipe(id = "1", title = "Pasta Carbonara", content = "Classic Italian pasta dish", timestamp = 0L),
                    Recipe(id = "2", title = "Chocolate Cake", content = "Rich and decadent cake", timestamp = 0L),
                ),
                order = RecipeOrder.Title(),
            ),
            onAddRecipe = {},
            onEditRecipe = {},
            onSync = {},
            onLogout = {},
            onDelete = {},
            onOrderChange = {},
        )
    }
}
