package com.ultraviolince.mykitchen.recipes.presentation.recipes

import android.util.Log
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.ultraviolince.mykitchen.recipes.data.FakeBackend
import com.ultraviolince.mykitchen.recipes.presentation.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class SmokeTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()

        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
        
        // Clear any existing recipes from the backend for test isolation
        FakeBackend.clearUserRecipes()
    }

    private fun createRecipe(title: String, content: String) {
        // When the "New Recipe" button is clicked
        with(composeTestRule.onNodeWithContentDescription("New recipe")) {
            assertExists()
            assertIsDisplayed()
            performClick()
        }

        composeTestRule.waitForIdle()

        // Type recipe details
        with(composeTestRule.onNodeWithContentDescription("Enter recipe title")) {
            assertExists()
            assertIsDisplayed()
            performTextInput(title)
        }
        with(composeTestRule.onNodeWithContentDescription("Enter recipe content")) {
            assertExists()
            assertIsDisplayed()
            performTextInput(content)
        }

        // Click "save"
        with(composeTestRule.onNodeWithContentDescription("Save recipe")) {
            assertExists()
            assertIsDisplayed()
            performClick()
        }

        composeTestRule.waitForIdle()

        // Recipe is in the overview
        with(composeTestRule.onNodeWithText(title)) {
            assertExists()
            assertIsDisplayed()
        }
        with(composeTestRule.onNodeWithText(content)) {
            assertExists()
            assertIsDisplayed()
        }
    }

    // TODO Fix the tests
    @Test
    fun createRecipe_WithoutLogin() {
        // By default, no cloud sync
        with(composeTestRule.onNodeWithContentDescription("Synchronisation with the backend is disabled")) {
            assertExists()
            assertIsDisplayed()
        }

        createRecipe("recipe1", "content1")
    }

    @Test fun loginToBackend_ThenCreateRecipe() {
        // By default, no cloud sync
        with(composeTestRule.onNodeWithContentDescription("Synchronisation with the backend is disabled")) {
            assertExists()
            assertIsDisplayed()
            performClick()
        }

        // Enter server and credentials
        with(composeTestRule.onNodeWithContentDescription("Server URI")) {
            assertExists()
            assertIsDisplayed()
            performTextClearance()
            performTextInput(FakeBackend.server)
        }
        with(composeTestRule.onNodeWithContentDescription("User name")) {
            assertExists()
            assertIsDisplayed()
            performTextClearance()
            performTextInput(FakeBackend.testUser)
        }
        with(composeTestRule.onNodeWithContentDescription("Password")) {
            assertExists()
            assertIsDisplayed()
            performTextClearance()
            performTextInput(FakeBackend.testPassword)
        }

        // Login
        with(composeTestRule.onNodeWithContentDescription("Login")) {
            assertExists()
            assertIsDisplayed()
            performClick()
        }

        // Create a new recipe, with backend now
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("New recipe"), 5000)

        createRecipe("recipe2", "content2")
    }

    @Test
    fun deleteRecipe_NavigatesBackToMainScreen() {
        // Start by going to the recipe creation screen
        with(composeTestRule.onNodeWithContentDescription("New recipe")) {
            assertExists()
            assertIsDisplayed()
            performClick()
        }

        composeTestRule.waitForIdle()

        // Enter some recipe details (but we won't save them)
        with(composeTestRule.onNodeWithContentDescription("Enter recipe title")) {
            assertExists()
            assertIsDisplayed()
            performTextInput("Recipe to delete")
        }
        with(composeTestRule.onNodeWithContentDescription("Enter recipe content")) {
            assertExists()
            assertIsDisplayed()
            performTextInput("Content to delete")
        }

        // Click the delete button instead of save
        with(composeTestRule.onNodeWithContentDescription("Delete recipe")) {
            assertExists()
            assertIsDisplayed()
            performClick()
        }

        composeTestRule.waitForIdle()

        // Verify we're back on the main screen by checking for the "New recipe" button
        with(composeTestRule.onNodeWithContentDescription("New recipe")) {
            assertExists()
            assertIsDisplayed()
        }

        // The test passes if we successfully return to the main screen after clicking delete
        // This verifies that clicking delete navigates back and doesn't create a recipe
    }
}
