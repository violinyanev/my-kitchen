package com.ultraviolince.mykitchen.recipes.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
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
class AddEditRecipeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        ApplicationProvider.getApplicationContext<Application>()
        val server = GlobalContext.get().get<InMemoryRecipesServer>()
        server.reset()
        server.seed(
            BackendRecipe(id = 1, title = "Waffles", body = "Butter", timestamp = 1)
        )
    }

    @Test
    fun add_edit_delete_recipe_flow_with_screenshots() {
        val context = composeRule.activity

        // Login
        composeRule.onNodeWithContentDescription(context.getString(R.string.sync_disabled)).performClick()
        composeRule.onNodeWithContentDescription("Login").performClick()
        composeRule.onNodeWithContentDescription(context.getString(R.string.sync_enabled)).assertIsDisplayed()

        // Add new recipe via FAB
        composeRule.onNodeWithContentDescription("New recipe").performClick()
        composeRule.onNodeWithContentDescription("Enter recipe title").assertIsDisplayed()
        composeRule.onRoot().captureRoboImage("edit/01_add_empty.png")

        composeRule.onNodeWithContentDescription("Enter recipe title").performTextInput("Toast")
        composeRule.onNodeWithContentDescription("Enter recipe content").performTextInput("Bread, Butter")
        composeRule.onRoot().captureRoboImage("edit/02_add_filled.png")
        composeRule.onNodeWithContentDescription("Save recipe").performClick()

        // Back on list - verify new appears
        composeRule.onNodeWithText("Toast").assertIsDisplayed()
        composeRule.onRoot().captureRoboImage("edit/03_list_after_add.png")

        // Open existing recipe for edit (Waffles)
        composeRule.onNodeWithText("Waffles").performClick()
        composeRule.onNodeWithContentDescription("Enter recipe title").assertIsDisplayed()
        composeRule.onRoot().captureRoboImage("edit/04_edit_existing.png")

        // Delete it
        composeRule.onNodeWithContentDescription("Delete recipe").performClick()
        composeRule.onRoot().captureRoboImage("edit/05_list_after_delete.png")
    }
}

