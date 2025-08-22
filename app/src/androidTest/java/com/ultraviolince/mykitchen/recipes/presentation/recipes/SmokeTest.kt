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
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("New recipe"), 15000)
        
        // Ensure we're in a clean state by logging out if needed
        try {
            // If we find the enabled sync button, we're logged in - need to access logout
            composeTestRule.onNodeWithContentDescription("Synchronisation with the backend is enabled")
                .assertExists()
            
            // Navigate to sync settings to access logout
            composeTestRule.onNodeWithContentDescription("Synchronisation with the backend is enabled")
                .performClick()
            composeTestRule.waitForIdle()
            
            // Look for logout button and click it
            composeTestRule.waitUntilExactlyOneExists(hasContentDescription("Logout"), 5000)
            composeTestRule.onNodeWithContentDescription("Logout")
                .performClick()
            
            // Wait for logout to complete and return to recipe list
            composeTestRule.waitUntilExactlyOneExists(hasContentDescription("New recipe"), 15000)
            
            // Wait for UI to stabilize after logout
            composeTestRule.waitForIdle()
        } catch (e: AssertionError) {
            // Already logged out or sync not enabled, continue
            composeTestRule.waitForIdle()
        }
    }

    private fun assertNodeDoesNotExist(text: String, errorMessage: String) {
        try {
            composeTestRule.onNodeWithText(text).assertExists()
            throw AssertionError(errorMessage)
        } catch (e: AssertionError) {
            // Expected - node should not exist
            if (e.message == errorMessage) {
                throw e
            }
            // Otherwise, this is the expected "node not found" error
        }
    }

    private fun createRecipe(title: String, content: String) {
        // Ensure we start from the recipe list
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("New recipe"), 10000)
        
        // When the "New Recipe" button is clicked
        composeTestRule.onNodeWithContentDescription("New recipe")
            .assertExists()
        composeTestRule.onNodeWithContentDescription("New recipe")
            .performClick()

        composeTestRule.waitForIdle()

        // Wait for the edit screen to fully load
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("Enter recipe title"), 10000)

        // Type recipe details - title
        composeTestRule.onNodeWithContentDescription("Enter recipe title")
            .assertExists()
        composeTestRule.onNodeWithContentDescription("Enter recipe title")
            .performTextInput(title)
        
        composeTestRule.waitForIdle()
        
        // Type recipe details - content
        composeTestRule.onNodeWithContentDescription("Enter recipe content")
            .assertExists()
        composeTestRule.onNodeWithContentDescription("Enter recipe content")
            .performTextInput(content)

        composeTestRule.waitForIdle()

        // Click "save"
        composeTestRule.onNodeWithContentDescription("Save recipe")
            .assertExists()
        composeTestRule.onNodeWithContentDescription("Save recipe")
            .performClick()

        // Wait for navigation back to the list with longer timeout for save operation
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("New recipe"), 15000)

        // Wait for the recipe to appear in the list
        composeTestRule.waitForIdle()

        // Verify recipe is in the overview
        composeTestRule.onNodeWithText(title)
            .assertExists()
        composeTestRule.onNodeWithText(content)
            .assertExists()
    }

    @Test
    fun createRecipe_WithoutLogin() {
        // By default, no cloud sync
        composeTestRule.onNodeWithContentDescription("Synchronisation with the backend is disabled")
            .assertExists()

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
        composeTestRule.onNodeWithText("Recipe to Delete")
            .assertExists()
        composeTestRule.onNodeWithText("Recipe to Delete")
            .performClick()

        // Wait for edit screen to load
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("Delete recipe"), 10000)

        // Click the delete button
        composeTestRule.onNodeWithContentDescription("Delete recipe")
            .assertExists()
        composeTestRule.onNodeWithContentDescription("Delete recipe")
            .performClick()

        // Wait for navigation back to recipe list
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("New recipe"), 10000)

        // Wait for the UI to update after deletion
        composeTestRule.waitForIdle()

        // Verify we're back at the recipe list and the recipe is gone
        assertNodeDoesNotExist("Recipe to Delete", "Recipe should have been deleted but still exists")
        assertNodeDoesNotExist("This recipe will be deleted", "Recipe content should have been deleted but still exists")
    }

    @Test
    fun startNewRecipe_ThenAbortWithDeleteButton() {
        // Click "New Recipe" button
        composeTestRule.onNodeWithContentDescription("New recipe")
            .assertExists()
        composeTestRule.onNodeWithContentDescription("New recipe")
            .performClick()

        // Wait for edit screen to load
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("Delete recipe"), 10000)

        // Enter some content
        composeTestRule.onNodeWithContentDescription("Enter recipe title")
            .assertExists()
        composeTestRule.onNodeWithContentDescription("Enter recipe title")
            .performTextInput("Aborted Recipe")
        
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithContentDescription("Enter recipe content")
            .assertExists()
        composeTestRule.onNodeWithContentDescription("Enter recipe content")
            .performTextInput("This should not be saved")

        composeTestRule.waitForIdle()

        // Click delete to abort
        composeTestRule.onNodeWithContentDescription("Delete recipe")
            .assertExists()
        composeTestRule.onNodeWithContentDescription("Delete recipe")
            .performClick()

        // Wait for navigation back to recipe list
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("New recipe"), 10000)

        // Wait for the UI to update
        composeTestRule.waitForIdle()

        // Verify we're back at the recipe list and the recipe was not created
        assertNodeDoesNotExist("Aborted Recipe", "Aborted recipe should not exist but was found")
        assertNodeDoesNotExist("This should not be saved", "Aborted recipe content should not exist but was found")
    }

    @Test
    fun startNewRecipe_ThenNavigateBack() {
        // Click "New Recipe" button
        composeTestRule.onNodeWithContentDescription("New recipe")
            .assertExists()
        composeTestRule.onNodeWithContentDescription("New recipe")
            .performClick()

        // Wait for edit screen to load
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("Delete recipe"), 10000)

        // Enter some content
        composeTestRule.onNodeWithContentDescription("Enter recipe title")
            .assertExists()
        composeTestRule.onNodeWithContentDescription("Enter recipe title")
            .performTextInput("Back Navigation Recipe")
        
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithContentDescription("Enter recipe content")
            .assertExists()
        composeTestRule.onNodeWithContentDescription("Enter recipe content")
            .performTextInput("This should not be saved either")

        composeTestRule.waitForIdle()

        // Navigate back using system back button
        pressBack()

        // Wait for navigation back to recipe list
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("New recipe"), 10000)

        // Wait for the UI to update
        composeTestRule.waitForIdle()

        // Verify we're back at the recipe list and the recipe was not created
        assertNodeDoesNotExist("Back Navigation Recipe", "Back navigation recipe should not exist but was found")
        assertNodeDoesNotExist("This should not be saved either", "Back navigation recipe content should not exist but was found")
    }

    @Test
    fun loginCreateRecipe_LogoutDeleteRecipe_LoginAgain_VerifyDeleted() {
        // First login to backend
        loginToBackend()

        // Verify we're logged in
        composeTestRule.onNodeWithContentDescription("Synchronisation with the backend is enabled")
            .assertExists()

        // Create a recipe while logged in
        createRecipe("Server Sync Recipe", "This recipe should sync to server")

        // Logout - navigate to sync settings
        composeTestRule.onNodeWithContentDescription("Synchronisation with the backend is enabled")
            .assertExists()
        composeTestRule.onNodeWithContentDescription("Synchronisation with the backend is enabled")
            .performClick()

        composeTestRule.waitForIdle()

        // Click logout
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("Logout"), 10000)
        composeTestRule.onNodeWithContentDescription("Logout")
            .assertExists()
        composeTestRule.onNodeWithContentDescription("Logout")
            .performClick()

        // Wait for logout to complete and return to recipe list
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("New recipe"), 20000)
        composeTestRule.waitForIdle()

        // Verify we're logged out
        composeTestRule.onNodeWithContentDescription("Synchronisation with the backend is disabled")
            .assertExists()

        // Delete the recipe locally (while logged out)
        composeTestRule.onNodeWithText("Server Sync Recipe")
            .assertExists()
        composeTestRule.onNodeWithText("Server Sync Recipe")
            .performClick()

        // Wait for edit screen to load
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("Delete recipe"), 10000)

        composeTestRule.onNodeWithContentDescription("Delete recipe")
            .assertExists()
        composeTestRule.onNodeWithContentDescription("Delete recipe")
            .performClick()

        // Wait for navigation back to recipe list
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("New recipe"), 10000)
        composeTestRule.waitForIdle()

        // Verify recipe is deleted locally
        assertNodeDoesNotExist("Server Sync Recipe", "Recipe should be deleted locally but still exists")

        // Login again with longer timeout for sync
        loginToBackend(30000)

        // Wait for sync operations to complete
        composeTestRule.waitForIdle()

        // Verify the recipe is still deleted (should not appear after sync)
        assertNodeDoesNotExist("Server Sync Recipe", "Recipe should remain deleted after sync but was found")
        assertNodeDoesNotExist("This recipe should sync to server", "Recipe content should remain deleted after sync but was found")
    }

    @Test
    fun createRecipe_ThenLoginToServer_VerifySynchronized() {
        // Create a recipe without being logged in
        createRecipe("Local Recipe", "This should sync when we login")

        // Verify we're logged out initially
        composeTestRule.onNodeWithContentDescription("Synchronisation with the backend is disabled")
            .assertExists()

        // Login to backend with longer timeout for sync
        loginToBackend(30000)

        // Verify we're now logged in
        composeTestRule.onNodeWithContentDescription("Synchronisation with the backend is enabled")
            .assertExists()

        // Wait for sync operations to complete
        composeTestRule.waitForIdle()

        // Verify the recipe is still there after login (it should have synced to server)
        composeTestRule.onNodeWithText("Local Recipe")
            .assertExists()
        composeTestRule.onNodeWithText("This should sync when we login")
            .assertExists()
    }

    private fun loginToBackend(timeout: Long = 25000) {
        // Ensure we start from a logged-out state
        composeTestRule.waitForIdle()
        
        // Click on sync settings
        composeTestRule.onNodeWithContentDescription("Synchronisation with the backend is disabled")
            .assertExists()
        composeTestRule.onNodeWithContentDescription("Synchronisation with the backend is disabled")
            .performClick()

        // Wait for login screen to load with longer timeout
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("Server URI"), 10000)
        composeTestRule.waitForIdle()

        // Clear and enter server URI
        composeTestRule.onNodeWithContentDescription("Server URI")
            .assertExists()
        composeTestRule.onNodeWithContentDescription("Server URI")
            .performTextClearance()
        composeTestRule.onNodeWithContentDescription("Server URI")
            .performTextInput(FakeBackend.server)
        
        composeTestRule.waitForIdle()

        // Clear and enter username
        composeTestRule.onNodeWithContentDescription("User name")
            .assertExists()
        composeTestRule.onNodeWithContentDescription("User name")
            .performTextClearance()
        composeTestRule.onNodeWithContentDescription("User name")
            .performTextInput(FakeBackend.testUser)
        
        composeTestRule.waitForIdle()

        // Clear and enter password
        composeTestRule.onNodeWithContentDescription("Password")
            .assertExists()
        composeTestRule.onNodeWithContentDescription("Password")
            .performTextClearance()
        composeTestRule.onNodeWithContentDescription("Password")
            .performTextInput(FakeBackend.testPassword)

        composeTestRule.waitForIdle()

        // Click login button
        composeTestRule.onNodeWithContentDescription("Login")
            .assertExists()
        composeTestRule.onNodeWithContentDescription("Login")
            .performClick()

        // Wait for login to complete and return to recipe list
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("New recipe"), timeout)
        
        // Wait for any sync operations to complete
        composeTestRule.waitForIdle()
    }
}
