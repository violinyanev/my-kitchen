package com.ultraviolince.mykitchen

import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.ultraviolince.mykitchen.domain.model.Recipe
import com.ultraviolince.mykitchen.domain.model.RecipeOrder
import com.ultraviolince.mykitchen.ui.screens.recipelist.RecipeListScreenContent
import com.ultraviolince.mykitchen.ui.screens.recipelist.RecipeListState
import com.ultraviolince.mykitchen.ui.theme.AppTheme
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.setResourceReaderAndroidContext
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(ScreenshotTestRunner::class)
class RecipeListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @OptIn(ExperimentalResourceApi::class)
    @Test
    fun withRecipes_topBarSyncAndLogoutAndItemsVisible() {
        composeTestRule.setContent {
            val ctx = LocalContext.current
            remember(ctx) { setResourceReaderAndroidContext(ctx) }
            AppTheme {
                RecipeListScreenContent(
                    state = RecipeListState(
                        recipes = listOf(
                            Recipe(id = "1", title = "Pasta Carbonara", content = "Classic pasta", timestamp = 0L),
                            Recipe(id = "2", title = "Chocolate Cake", content = "Rich cake", timestamp = 0L),
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

        composeTestRule.onNodeWithContentDescription("Sync").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Logout").assertIsDisplayed()
        composeTestRule.onNodeWithText("Pasta Carbonara").assertIsDisplayed()
        composeTestRule.onNodeWithText("Chocolate Cake").assertIsDisplayed()
    }

    @OptIn(ExperimentalResourceApi::class)
    @Test
    fun emptyState_topBarAndNoRecipesTextVisible() {
        composeTestRule.setContent {
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

        composeTestRule.onNodeWithContentDescription("Sync").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Logout").assertIsDisplayed()
        composeTestRule.onNodeWithText("No recipes yet. Tap + to add one.").assertIsDisplayed()
    }

    @OptIn(ExperimentalResourceApi::class)
    @Test
    fun serverUnreachable_bannerIsShown() {
        composeTestRule.setContent {
            val ctx = LocalContext.current
            remember(ctx) { setResourceReaderAndroidContext(ctx) }
            AppTheme {
                RecipeListScreenContent(
                    state = RecipeListState(isServerReachable = false),
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

        composeTestRule.onNodeWithText(
            "Server unreachable — adding recipes is disabled until sync succeeds.",
            substring = true,
        ).assertIsDisplayed()
    }

    @OptIn(ExperimentalResourceApi::class)
    @Test
    fun serverUnreachable_fabClickDoesNotTriggerNavigation() {
        var addRecipeCalled = false
        composeTestRule.setContent {
            val ctx = LocalContext.current
            remember(ctx) { setResourceReaderAndroidContext(ctx) }
            AppTheme {
                RecipeListScreenContent(
                    state = RecipeListState(isServerReachable = false),
                    onAddRecipe = { addRecipeCalled = true },
                    onEditRecipe = {},
                    onSync = {},
                    onLogout = {},
                    onDelete = {},
                    onOrderChange = {},
                    onTagSelect = {},
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Add recipe").performClick()
        assertFalse("FAB should not navigate when server is unreachable", addRecipeCalled)
    }

    @OptIn(ExperimentalResourceApi::class)
    @Test
    fun serverReachable_fabClickTriggersNavigation() {
        var addRecipeCalled = false
        composeTestRule.setContent {
            val ctx = LocalContext.current
            remember(ctx) { setResourceReaderAndroidContext(ctx) }
            AppTheme {
                RecipeListScreenContent(
                    state = RecipeListState(isServerReachable = true),
                    onAddRecipe = { addRecipeCalled = true },
                    onEditRecipe = {},
                    onSync = {},
                    onLogout = {},
                    onDelete = {},
                    onOrderChange = {},
                    onTagSelect = {},
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Add recipe").performClick()
        assertTrue("FAB should navigate when server is reachable", addRecipeCalled)
    }
}
