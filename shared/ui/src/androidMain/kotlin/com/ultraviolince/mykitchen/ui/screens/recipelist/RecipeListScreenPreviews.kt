package com.ultraviolince.mykitchen.ui.screens.recipelist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.ultraviolince.mykitchen.domain.model.Recipe
import com.ultraviolince.mykitchen.domain.model.RecipeOrder
import com.ultraviolince.mykitchen.ui.theme.AppTheme
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.setResourceReaderAndroidContext

@OptIn(ExperimentalResourceApi::class)
@Preview(showBackground = true)
@Composable
internal fun RecipeListScreenEmptyPreview() {
    val ctx = LocalContext.current
    remember(ctx) { setResourceReaderAndroidContext(ctx) }
    AppTheme {
        RecipeListScreenContent(
            state = RecipeListState(),
            onAddRecipe = {},
            onEditRecipe = {},
            onSync = {},
            onLogout = {},
            onDelete = {},
            onOrderChange = {},
            onTagSelect = {},
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
@Preview(showBackground = true, name = "Recipe List With Items")
@Composable
internal fun RecipeListScreenWithItemsPreview() {
    val ctx = LocalContext.current
    remember(ctx) { setResourceReaderAndroidContext(ctx) }
    AppTheme {
        RecipeListScreenContent(
            state = RecipeListState(
                recipes = listOf(
                    Recipe(id = "1", title = "Pasta Carbonara", content = "Classic Italian pasta dish", timestamp = 0L),
                    Recipe(id = "2", title = "Chocolate Cake", content = "Rich and decadent cake", timestamp = 0L),
                ),
                order = RecipeOrder.Title(),
                tagsByRecipe = mapOf(
                    "1" to listOf("quick", "budget-friendly"),
                    "2" to listOf("kids-friendly"),
                ),
            ),
            onAddRecipe = {},
            onEditRecipe = {},
            onSync = {},
            onLogout = {},
            onDelete = {},
            onOrderChange = {},
            onTagSelect = {},
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
@Preview(showBackground = true, name = "Recipe List — Server Unreachable")
@Composable
internal fun RecipeListScreenServerUnreachablePreview() {
    val ctx = LocalContext.current
    remember(ctx) { setResourceReaderAndroidContext(ctx) }
    AppTheme {
        RecipeListScreenContent(
            state = RecipeListState(
                recipes = listOf(
                    Recipe(id = "1", title = "Pasta Carbonara", content = "Classic Italian pasta dish", timestamp = 0L),
                ),
                isServerReachable = false,
            ),
            onAddRecipe = {},
            onEditRecipe = {},
            onSync = {},
            onLogout = {},
            onDelete = {},
            onOrderChange = {},
            onTagSelect = {},
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
@Preview(showBackground = true, name = "Phone — Recipe List", widthDp = 360, heightDp = 800)
@Composable
internal fun RecipeListScreenPhonePreview() {
    val ctx = LocalContext.current
    remember(ctx) { setResourceReaderAndroidContext(ctx) }
    AppTheme {
        RecipeListScreenContent(
            state = RecipeListState(
                recipes = listOf(
                    Recipe(id = "1", title = "Pasta Carbonara", content = "Classic Italian pasta dish", timestamp = 0L),
                    Recipe(id = "2", title = "Chocolate Cake", content = "Rich and decadent cake", timestamp = 0L),
                    Recipe(id = "3", title = "Caesar Salad", content = "Fresh and crispy", timestamp = 0L),
                ),
                order = RecipeOrder.Date(),
            ),
            onAddRecipe = {},
            onEditRecipe = {},
            onSync = {},
            onLogout = {},
            onDelete = {},
            onOrderChange = {},
            onTagSelect = {},
        )
    }
}
