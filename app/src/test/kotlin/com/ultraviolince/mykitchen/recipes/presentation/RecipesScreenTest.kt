package com.ultraviolince.mykitchen.recipes.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import android.app.Application
import com.github.takahirom.roborazzi.captureRoboImage
import com.ultraviolince.mykitchen.R
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.data.BackendRecipe
import com.ultraviolince.mykitchen.testutil.InMemoryRecipesServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext
import org.robolectric.annotation.Config
import com.ultraviolince.mykitchen.ui.theme.MyApplicationTheme

@RunWith(com.ultraviolince.mykitchen.utils.RoborazziTestRunner::class)
@Config(sdk = [34])
class RecipesScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        // Seed the Koin-provided in-memory server
        ApplicationProvider.getApplicationContext<Application>() // Ensure Application (and Koin) is created
        val server = GlobalContext.get().get<InMemoryRecipesServer>()
        server.reset()
        server.seed(
            BackendRecipe(id = 1, title = "Pancakes", body = "Flour, Eggs, Milk", timestamp = 1),
            BackendRecipe(id = 2, title = "Salad", body = "Tomato, Cucumber", timestamp = 2)
        )
    }

    @Test
    fun recipes_list_shows_items_and_screenshots() {
        val context = composeRule.activity

        // Initially sync icon is disabled (not logged in)
        composeRule.onNodeWithContentDescription(context.getString(R.string.sync_disabled)).assertIsDisplayed()
        composeRule.onRoot().captureRoboImage("recipes/01_initial_sync_disabled.png")

        // Navigate to login
        composeRule.onAllNodesWithContentDescription(context.getString(R.string.sort)).get(0).assertIsDisplayed()
        composeRule.onNodeWithContentDescription("New recipe").assertIsDisplayed()
        composeRule.onNodeWithContentDescription(context.getString(R.string.sync_disabled)).performClick()

        // On login screen enter text fields (defaults are prefilled, just click login)
        composeRule.onNodeWithContentDescription("Login").assertIsDisplayed()
        composeRule.onRoot().captureRoboImage("recipes/02_login_screen.png")
        composeRule.onNodeWithContentDescription("Login").performClick()

        // After login, back on recipes, wait for items to appear
        composeRule.waitUntil(timeoutMillis = 15_000) {
            try {
                composeRule.onNodeWithText("Pancakes").fetchSemanticsNode()
                composeRule.onNodeWithText("Salad").fetchSemanticsNode()
                true
            } catch (t: Throwable) { false }
        }
        composeRule.onNodeWithText("Pancakes").assertIsDisplayed()
        composeRule.onNodeWithText("Salad").assertIsDisplayed()
        composeRule.onRoot().captureRoboImage("recipes/03_recipes_after_login.png")

        // Open sort dialog/section
        composeRule.onAllNodesWithContentDescription(context.getString(R.string.sort)).get(0).performClick()
        composeRule.onNodeWithText(context.getString(R.string.sort_by_heading)).assertIsDisplayed()
        composeRule.onRoot().captureRoboImage("recipes/04_sort_open.png")
    }
}

