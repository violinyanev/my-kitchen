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
import androidx.test.espresso.Espresso.pressBack
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
        
        // Clear any existing app data and reset to initial state
        composeTestRule.activityRule.scenario.recreate()
        
        // Wait for the app to be ready before each test with longer timeout
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("New recipe"), 10000)
        
        // Ensure we're in a clean state by logging out if needed
        try {
            composeTestRule.onNodeWithContentDescription("Synchronisation with the backend is enabled")
                .assertExists()
            // If we find the enabled sync button, logout first
            composeTestRule.onNodeWithContentDescription("Synchronisation with the backend is enabled")
                .performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithContentDescription("Logout")
                .performClick()
            composeTestRule.waitUntilExactlyOneExists(hasContentDescription("New recipe"), 10000)
        } catch (e: AssertionError) {
            // Already logged out or sync not enabled, continue
        }
    }

    private fun createRecipe(title: String, content: String) {
        // When the "New Recipe" button is clicked
        with(composeTestRule.onNodeWithContentDescription("New recipe")) {
            assertExists()
            assertIsDisplayed()
            performClick()
        }

        composeTestRule.waitForIdle()

        // Wait for the edit screen to fully load
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("Enter recipe title"), 8000)

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

        composeTestRule.waitForIdle()

        // Click "save"
        with(composeTestRule.onNodeWithContentDescription("Save recipe")) {
            assertExists()
            assertIsDisplayed()
            performClick()
        }

        // Wait for navigation back to the list with longer timeout for save operation
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("New recipe"), 10000)

        // Wait a bit more for the recipe to appear in the list
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

    @Test
    fun createRecipe_WithoutLogin() {
        // By default, no cloud sync
        with(composeTestRule.onNodeWithContentDescription("Synchronisation with the backend is disabled")) {
            assertExists()
            assertIsDisplayed()
        }

        createRecipe("recipe1", "content1")
    }

    @Test
    fun loginToBackend_ThenCreateRecipe() {
        // By default, no cloud sync
        loginToBackend()
        createRecipe("recipe2", "content2")
    }

    @Test
    fun deleteExistingRecipe() {
        // First create a recipe
        createRecipe("Recipe to Delete", "This recipe will be deleted")

        // Click on the created recipe to edit it
        with(composeTestRule.onNodeWithText("Recipe to Delete")) {
            assertExists()
            assertIsDisplayed()
            performClick()
        }

        // Wait for edit screen to load
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("Delete recipe"), 8000)

        // Click the delete button
        with(composeTestRule.onNodeWithContentDescription("Delete recipe")) {
            assertExists()
            assertIsDisplayed()
            performClick()
        }

        // Wait for navigation back to recipe list
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("New recipe"), 8000)

        // Wait for the UI to update after deletion
        composeTestRule.waitForIdle()

        // Verify we're back at the recipe list and the recipe is gone
        with(composeTestRule.onNodeWithText("Recipe to Delete")) {
            assertDoesNotExist()
        }
        with(composeTestRule.onNodeWithText("This recipe will be deleted")) {
            assertDoesNotExist()
        }
    }

    @Test
    fun startNewRecipe_ThenAbortWithDeleteButton() {
        // Click "New Recipe" button
        with(composeTestRule.onNodeWithContentDescription("New recipe")) {
            assertExists()
            assertIsDisplayed()
            performClick()
        }

        // Wait for edit screen to load
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("Delete recipe"), 8000)

        // Enter some content
        with(composeTestRule.onNodeWithContentDescription("Enter recipe title")) {
            assertExists()
            assertIsDisplayed()
            performTextInput("Aborted Recipe")
        }
        with(composeTestRule.onNodeWithContentDescription("Enter recipe content")) {
            assertExists()
            assertIsDisplayed()
            performTextInput("This should not be saved")
        }

        composeTestRule.waitForIdle()

        // Click delete to abort
        with(composeTestRule.onNodeWithContentDescription("Delete recipe")) {
            assertExists()
            assertIsDisplayed()
            performClick()
        }

        // Wait for navigation back to recipe list
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("New recipe"), 8000)

        // Wait for the UI to update
        composeTestRule.waitForIdle()

        // Verify we're back at the recipe list and the recipe was not created
        with(composeTestRule.onNodeWithText("Aborted Recipe")) {
            assertDoesNotExist()
        }
        with(composeTestRule.onNodeWithText("This should not be saved")) {
            assertDoesNotExist()
        }
    }

    @Test
    fun startNewRecipe_ThenNavigateBack() {
        // Click "New Recipe" button
        with(composeTestRule.onNodeWithContentDescription("New recipe")) {
            assertExists()
            assertIsDisplayed()
            performClick()
        }

        // Wait for edit screen to load
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("Delete recipe"), 8000)

        // Enter some content
        with(composeTestRule.onNodeWithContentDescription("Enter recipe title")) {
            assertExists()
            assertIsDisplayed()
            performTextInput("Back Navigation Recipe")
        }
        with(composeTestRule.onNodeWithContentDescription("Enter recipe content")) {
            assertExists()
            assertIsDisplayed()
            performTextInput("This should not be saved either")
        }

        composeTestRule.waitForIdle()

        // Navigate back using system back button
        pressBack()

        // Wait for navigation back to recipe list
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("New recipe"), 8000)

        // Wait for the UI to update
        composeTestRule.waitForIdle()

        // Verify we're back at the recipe list and the recipe was not created
        with(composeTestRule.onNodeWithText("Back Navigation Recipe")) {
            assertDoesNotExist()
        }
        with(composeTestRule.onNodeWithText("This should not be saved either")) {
            assertDoesNotExist()
        }
    }

    @Test
    fun loginCreateRecipe_LogoutDeleteRecipe_LoginAgain_VerifyDeleted() {
        // First login to backend
        loginToBackend()

        // Create a recipe while logged in
        createRecipe("Server Sync Recipe", "This recipe should sync to server")

        // Logout
        with(composeTestRule.onNodeWithContentDescription("Synchronisation with the backend is enabled")) {
            assertExists()
            assertIsDisplayed()
            performClick()
        }

        composeTestRule.waitForIdle()

        with(composeTestRule.onNodeWithContentDescription("Logout")) {
            assertExists()
            assertIsDisplayed()
            performClick()
        }

        // Wait for logout to complete and return to recipe list with extended timeout
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("New recipe"), 15000)

        // Delete the recipe locally (while logged out)
        with(composeTestRule.onNodeWithText("Server Sync Recipe")) {
            assertExists()
            assertIsDisplayed()
            performClick()
        }

        // Wait for edit screen to load
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("Delete recipe"), 8000)

        with(composeTestRule.onNodeWithContentDescription("Delete recipe")) {
            assertExists()
            assertIsDisplayed()
            performClick()
        }

        // Wait for navigation back to recipe list
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("New recipe"), 8000)

        // Login again with longer timeout for sync
        loginToBackend(25000)

        // Wait for sync operations to complete
        composeTestRule.waitForIdle()

        // Verify the recipe is still deleted (should not appear after sync)
        with(composeTestRule.onNodeWithText("Server Sync Recipe")) {
            assertDoesNotExist()
        }
        with(composeTestRule.onNodeWithText("This recipe should sync to server")) {
            assertDoesNotExist()
        }
    }

    @Test
    fun createRecipe_ThenLoginToServer_VerifySynchronized() {
        // Create a recipe without being logged in
        createRecipe("Local Recipe", "This should sync when we login")

        // Login to backend with longer timeout for sync
        loginToBackend(25000)

        // Wait for sync operations to complete
        composeTestRule.waitForIdle()

        // Verify the recipe is still there after login (it should have synced to server)
        with(composeTestRule.onNodeWithText("Local Recipe")) {
            assertExists()
            assertIsDisplayed()
        }
        with(composeTestRule.onNodeWithText("This should sync when we login")) {
            assertExists()
            assertIsDisplayed()
        }

        // Verify we're now logged in (the sync icon should show as enabled)
        with(composeTestRule.onNodeWithContentDescription("Synchronisation with the backend is enabled")) {
            assertExists()
            assertIsDisplayed()
        }
    }

    private fun loginToBackend(timeout: Long = 20000) {
        // Click on sync settings
        with(composeTestRule.onNodeWithContentDescription("Synchronisation with the backend is disabled")) {
            assertExists()
            assertIsDisplayed()
            performClick()
        }

        // Wait for login screen to load
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("Server URI"), 8000)

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

        composeTestRule.waitForIdle()

        // Login
        with(composeTestRule.onNodeWithContentDescription("Login")) {
            assertExists()
            assertIsDisplayed()
            performClick()
        }

        // Wait for login to complete
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("New recipe"), timeout)
    }
}
