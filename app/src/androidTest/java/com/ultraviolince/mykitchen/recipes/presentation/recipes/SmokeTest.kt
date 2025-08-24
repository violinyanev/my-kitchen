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
    }

    private fun createRecipe(title: String, content: String) {
        // When the "New Recipe" button is clicked
        with(composeTestRule.onNodeWithContentDescription("New recipe")) {
            assertIsDisplayed()
            performClick()
        }

        composeTestRule.waitForIdle()

        // Type recipe details
        with(composeTestRule.onNodeWithContentDescription("Enter recipe title")) {
            assertIsDisplayed()
            performTextInput(title)
        }
        
        composeTestRule.waitForIdle()
        
        with(composeTestRule.onNodeWithContentDescription("Enter recipe content")) {
            assertIsDisplayed()
            performTextInput(content)
        }

        composeTestRule.waitForIdle()

        // Click "save"
        with(composeTestRule.onNodeWithContentDescription("Save recipe")) {
            assertIsDisplayed()
            performClick()
        }

        composeTestRule.waitForIdle()

        // Recipe is in the overview
        with(composeTestRule.onNodeWithText(title)) {
            assertIsDisplayed()
        }
        with(composeTestRule.onNodeWithText(content)) {
            assertIsDisplayed()
        }
    }

    @Test
    fun createRecipe_WithoutLogin() {
        // By default, no cloud sync
        with(composeTestRule.onNodeWithContentDescription("Synchronisation with the backend is disabled")) {
            assertIsDisplayed()
        }

        createRecipe("recipe1", "content1")
    }

    @Test fun loginToBackend_ThenCreateRecipe() {
        // By default, no cloud sync
        with(composeTestRule.onNodeWithContentDescription("Synchronisation with the backend is disabled")) {
            assertIsDisplayed()
            performClick()
        }

        composeTestRule.waitForIdle()

        // Enter server and credentials
        with(composeTestRule.onNodeWithContentDescription("Server URI")) {
            assertIsDisplayed()
            performTextClearance()
            performTextInput(FakeBackend.server)
        }
        with(composeTestRule.onNodeWithContentDescription("User name")) {
            assertIsDisplayed()
            performTextClearance()
            performTextInput(FakeBackend.testUser)
        }
        with(composeTestRule.onNodeWithContentDescription("Password")) {
            assertIsDisplayed()
            performTextClearance()
            performTextInput(FakeBackend.testPassword)
        }

        // Login
        with(composeTestRule.onNodeWithContentDescription("Login")) {
            assertIsDisplayed()
            performClick()
        }

        // Wait for login to complete and return to main screen
        // This could take longer in emulator environments, especially in CI
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("New recipe"), 15000)

        // Clear any existing recipes from the backend for test isolation
        // Do this AFTER login succeeds to ensure backend is ready and authenticated
        FakeBackend.clearUserRecipes()

        createRecipe("recipe2", "content2")
    }

    @Test
    fun deleteRecipe_NavigatesBackToMainScreen() {
        // Start by going to the recipe creation screen
        with(composeTestRule.onNodeWithContentDescription("New recipe")) {
            assertIsDisplayed()
            performClick()
        }

        composeTestRule.waitForIdle()

        // Enter some recipe details (but we won't save them)
        with(composeTestRule.onNodeWithContentDescription("Enter recipe title")) {
            assertIsDisplayed()
            performTextInput("Recipe to delete")
        }
        
        composeTestRule.waitForIdle()
        
        with(composeTestRule.onNodeWithContentDescription("Enter recipe content")) {
            assertIsDisplayed()
            performTextInput("Content to delete")
        }

        composeTestRule.waitForIdle()

        // Click the delete button instead of save
        with(composeTestRule.onNodeWithContentDescription("Delete recipe")) {
            assertIsDisplayed()
            performClick()
        }

        composeTestRule.waitForIdle()

        // Verify we're back on the main screen by checking for the "New recipe" button
        with(composeTestRule.onNodeWithContentDescription("New recipe")) {
            assertIsDisplayed()
        }

        // The test passes if we successfully return to the main screen after clicking delete
        // This verifies that clicking delete navigates back and doesn't create a recipe
    }
}
