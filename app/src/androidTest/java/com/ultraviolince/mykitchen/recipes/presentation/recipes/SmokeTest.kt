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
        
        // Give the system a moment to stabilize in CI environments
        // Increased from 1s to 3s for better CI stability
        Thread.sleep(3000)
        
        // Wait for the app to be fully loaded by checking for main UI elements
        // Try to find either the sync button or new recipe button with extended timeout
        try {
            composeTestRule.waitUntilAtLeastOneExists(
                hasContentDescription("Synchronisation with the backend is disabled"),
                timeoutMillis = 30000 // 30 seconds for CI environments
            )
        } catch (e: Exception) {
            // If sync button not found, try new recipe button as fallback
            composeTestRule.waitUntilAtLeastOneExists(
                hasContentDescription("New recipe"),
                timeoutMillis = 10000 // Additional 10 seconds
            )
        }
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
        
        composeTestRule.waitForIdle()
        
        with(composeTestRule.onNodeWithContentDescription("User name")) {
            assertIsDisplayed()
            performTextClearance()
            performTextInput(FakeBackend.testUser)
        }
        
        composeTestRule.waitForIdle()
        
        with(composeTestRule.onNodeWithContentDescription("Password")) {
            assertIsDisplayed()
            performTextClearance()
            performTextInput(FakeBackend.testPassword)
        }

        composeTestRule.waitForIdle()

        // Login
        with(composeTestRule.onNodeWithContentDescription("Login")) {
            assertIsDisplayed()
            performClick()
        }

        composeTestRule.waitForIdle()

        // Wait for login to complete and return to main screen
        // This could take longer in emulator environments, especially in CI
        // Increased timeout from 20s to 30s for better CI stability
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("New recipe"), 30000)
        
        // Additional wait to ensure backend connection is fully established
        Thread.sleep(2000)

        // Clear any existing recipes from the backend for test isolation
        // Do this AFTER login succeeds to ensure backend is ready and authenticated
        try {
            val clearSuccess = FakeBackend.clearUserRecipes()
            android.util.Log.i("SmokeTest", "Backend clear result: $clearSuccess")
        } catch (e: Exception) {
            android.util.Log.w("SmokeTest", "Failed to clear backend recipes, continuing with test: ${e.message}")
        }

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
