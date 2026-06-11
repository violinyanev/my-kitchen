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
        
        // Wait for the app to be ready with extended timeout
        composeTestRule.waitForIdle()
        
        // Give the app more time to initialize completely
        Thread.sleep(5000)
        composeTestRule.waitForIdle()
        
        // Wait for the main screen to load with much longer timeout - try multiple times
        var attempts = 0
        val maxAttempts = 3
        var success = false
        
        while (attempts < maxAttempts && !success) {
            try {
                attempts++
                
                // Wait for the new recipe button with extended timeout
                composeTestRule.waitUntilExactlyOneExists(hasContentDescription("New recipe"), 45000)
                success = true
            } catch (e: Exception) {
                if (attempts < maxAttempts) {
                    // Recreate activity and try again
                    composeTestRule.activityRule.scenario.recreate()
                    Thread.sleep(3000)
                    composeTestRule.waitForIdle()
                } else {
                    // Last attempt - try alternative approaches
                    try {
                        // Try to find any sync state first
                        try {
                            composeTestRule.waitUntilAtLeastOneExists(hasContentDescription("Synchronisation with the backend is disabled"), 15000)
                        } catch (e3: Exception) {
                            composeTestRule.waitUntilAtLeastOneExists(hasContentDescription("Synchronisation with the backend is enabled"), 15000)
                        }
                        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("New recipe"), 30000)
                        success = true
                    } catch (e2: Exception) {
                        throw RuntimeException("Failed to initialize app after $maxAttempts attempts. Last error: ${e2.message}")
                    }
                }
            }
        }
        
        // Ensure we're in a clean state by logging out if needed
        try {
            // Try to find the logout mechanism - first check if we're logged in
            composeTestRule.onNodeWithContentDescription("Synchronisation with the backend is enabled")
                .assertExists()
            
            // We're logged in, so logout
            performLogout()
        } catch (e: AssertionError) {
            // Not logged in, which is what we want
        }
        
        // Final wait for stability
        composeTestRule.waitForIdle()
    }
    
    private fun performLogout() {
        try {
            // Click on sync settings
            composeTestRule.onNodeWithContentDescription("Synchronisation with the backend is enabled")
                .performClick()
            composeTestRule.waitForIdle()
            
            // Wait for logout button and click it with extended timeout
            composeTestRule.waitUntilExactlyOneExists(hasContentDescription("Logout"), 30000)
            composeTestRule.onNodeWithContentDescription("Logout")
                .performClick()
            
            // Wait for logout to complete with extended timeout
            composeTestRule.waitUntilExactlyOneExists(hasContentDescription("New recipe"), 45000)
            composeTestRule.waitForIdle()
        } catch (e: Exception) {
            // If logout fails, try to navigate back to main screen
            try {
                pressBack()
                composeTestRule.waitUntilExactlyOneExists(hasContentDescription("New recipe"), 30000)
            } catch (e2: Exception) {
                // Last resort - recreate the activity and wait longer
                composeTestRule.activityRule.scenario.recreate()
                Thread.sleep(5000)
                composeTestRule.waitForIdle()
                composeTestRule.waitUntilExactlyOneExists(hasContentDescription("New recipe"), 45000)
            }
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
        // Ensure we start from the recipe list with extended timeout
        composeTestRule.waitForIdle()
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("New recipe"), 30000)
        
        // Click the "New Recipe" button
        composeTestRule.onNodeWithContentDescription("New recipe")
            .assertExists()
        composeTestRule.onNodeWithContentDescription("New recipe")
            .performClick()

        composeTestRule.waitForIdle()

        // Wait for the edit screen to fully load with extended timeout
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("Enter recipe title"), 45000)

        // Enter recipe title with wait
        composeTestRule.onNodeWithContentDescription("Enter recipe title")
            .assertExists()
        composeTestRule.onNodeWithContentDescription("Enter recipe title")
            .performTextInput(title)
        
        composeTestRule.waitForIdle()
        Thread.sleep(1000) // Allow UI to process input
        
        // Enter recipe content with wait
        composeTestRule.onNodeWithContentDescription("Enter recipe content")
            .assertExists()
        composeTestRule.onNodeWithContentDescription("Enter recipe content")
            .performTextInput(content)

        composeTestRule.waitForIdle()
        Thread.sleep(1000) // Allow UI to process input

        // Save the recipe
        composeTestRule.onNodeWithContentDescription("Save recipe")
            .assertExists()
        composeTestRule.onNodeWithContentDescription("Save recipe")
            .performClick()

        // Wait for navigation back to the list with extended timeout
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("New recipe"), 45000)
        composeTestRule.waitForIdle()
    }

    @Test
    fun createRecipe_WithoutLogin() {
        // Ensure we start in a clean state
        composeTestRule.waitForIdle()
        
        // By default, no cloud sync - verify we're logged out
        composeTestRule.onNodeWithContentDescription("Synchronisation with the backend is disabled")
            .assertExists()

        // Create a recipe while logged out
        createRecipe("recipe1", "content1")
        
        // Verify the recipe was created successfully
        composeTestRule.onNodeWithText("recipe1").assertExists()
        composeTestRule.onNodeWithText("content1").assertExists()
    }

    @Test
    fun loginToBackend_ThenCreateRecipe() {
        // Ensure we start in a clean state
        composeTestRule.waitForIdle()
        
        // Verify we start logged out
        composeTestRule.onNodeWithContentDescription("Synchronisation with the backend is disabled")
            .assertExists()

        // Login to backend
        loginToBackend()
        
        // Verify we're now logged in
        composeTestRule.onNodeWithContentDescription("Synchronisation with the backend is enabled")
            .assertExists()
        
        // Create a recipe while logged in
        createRecipe("recipe2", "content2")
        
        // Verify the recipe was created successfully
        composeTestRule.onNodeWithText("recipe2").assertExists()
        composeTestRule.onNodeWithText("content2").assertExists()
    }

    @Test
    fun deleteExistingRecipe() {
        // Ensure we start in a clean state
        composeTestRule.waitForIdle()
        
        // Wait for the new recipe button to be available
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("New recipe"), 20000)
        
        // First create a recipe
        createRecipe("Recipe to Delete", "This recipe will be deleted")

        // Verify the recipe was created
        composeTestRule.onNodeWithText("Recipe to Delete").assertExists()
        composeTestRule.onNodeWithText("This recipe will be deleted").assertExists()

        // Click on the created recipe to edit it
        composeTestRule.onNodeWithText("Recipe to Delete")
            .performClick()

        // Wait for edit screen to load with extended timeout
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("Delete recipe"), 30000)

        // Click the delete button
        composeTestRule.onNodeWithContentDescription("Delete recipe")
            .assertExists()
        composeTestRule.onNodeWithContentDescription("Delete recipe")
            .performClick()

        // Wait for navigation back to recipe list with extended timeout
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("New recipe"), 30000)
        composeTestRule.waitForIdle()

        // Verify the recipe is gone
        assertNodeDoesNotExist("Recipe to Delete", "Recipe should have been deleted but still exists")
        assertNodeDoesNotExist("This recipe will be deleted", "Recipe content should have been deleted but still exists")
    }

    @Test
    fun startNewRecipe_ThenAbortWithDeleteButton() {
        // Ensure we start in a clean state
        composeTestRule.waitForIdle()
        
        // Wait for the new recipe button to be available
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("New recipe"), 20000)
        
        // Click "New Recipe" button
        composeTestRule.onNodeWithContentDescription("New recipe")
            .assertExists()
        composeTestRule.onNodeWithContentDescription("New recipe")
            .performClick()

        // Wait for edit screen to load with extended timeout
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("Delete recipe"), 30000)

        // Enter some content
        composeTestRule.onNodeWithContentDescription("Enter recipe title")
            .assertExists()
        composeTestRule.onNodeWithContentDescription("Enter recipe title")
            .performTextInput("Aborted Recipe")
        
        composeTestRule.waitForIdle()
        Thread.sleep(500)
        
        composeTestRule.onNodeWithContentDescription("Enter recipe content")
            .assertExists()
        composeTestRule.onNodeWithContentDescription("Enter recipe content")
            .performTextInput("This should not be saved")

        composeTestRule.waitForIdle()
        Thread.sleep(500)

        // Click delete to abort
        composeTestRule.onNodeWithContentDescription("Delete recipe")
            .assertExists()
        composeTestRule.onNodeWithContentDescription("Delete recipe")
            .performClick()

        // Wait for navigation back to recipe list with extended timeout
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("New recipe"), 30000)
        composeTestRule.waitForIdle()

        // Verify the recipe was not created
        assertNodeDoesNotExist("Aborted Recipe", "Aborted recipe should not exist but was found")
        assertNodeDoesNotExist("This should not be saved", "Aborted recipe content should not exist but was found")
    }

    @Test
    fun startNewRecipe_ThenNavigateBack() {
        // Ensure we start in a clean state
        composeTestRule.waitForIdle()
        
        // Wait for the new recipe button to be available with extended timeout
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("New recipe"), 45000)
        
        // Click "New Recipe" button
        composeTestRule.onNodeWithContentDescription("New recipe")
            .assertExists()
        composeTestRule.onNodeWithContentDescription("New recipe")
            .performClick()

        // Wait for edit screen to load with extended timeout
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("Delete recipe"), 45000)

        // Enter some content
        composeTestRule.onNodeWithContentDescription("Enter recipe title")
            .assertExists()
        composeTestRule.onNodeWithContentDescription("Enter recipe title")
            .performTextInput("Back Navigation Recipe")
        
        composeTestRule.waitForIdle()
        Thread.sleep(1000)
        
        composeTestRule.onNodeWithContentDescription("Enter recipe content")
            .assertExists()
        composeTestRule.onNodeWithContentDescription("Enter recipe content")
            .performTextInput("This should not be saved either")

        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        // Navigate back using system back button
        pressBack()

        // Wait for navigation back to recipe list with extended timeout
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("New recipe"), 45000)
        composeTestRule.waitForIdle()

        // Verify the recipe was not created
        assertNodeDoesNotExist("Back Navigation Recipe", "Back navigation recipe should not exist but was found")
        assertNodeDoesNotExist("This should not be saved either", "Back navigation recipe content should not exist but was found")
    }

    @Test
    fun loginCreateRecipe_LogoutDeleteRecipe_LoginAgain_VerifyDeleted() {
        // Ensure we start in a clean state
        composeTestRule.waitForIdle()
        
        // Wait for the new recipe button to be available with extended timeout
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("New recipe"), 45000)
        
        // First login to backend
        loginToBackend()

        // Verify we're logged in
        composeTestRule.onNodeWithContentDescription("Synchronisation with the backend is enabled")
            .assertExists()

        // Create a recipe while logged in
        createRecipe("Server Sync Recipe", "This recipe should sync to server")

        // Verify the recipe was created
        composeTestRule.onNodeWithText("Server Sync Recipe").assertExists()

        // Logout using the helper function
        performLogout()

        // Verify we're logged out
        composeTestRule.onNodeWithContentDescription("Synchronisation with the backend is disabled")
            .assertExists()

        // Delete the recipe locally (while logged out)
        composeTestRule.onNodeWithText("Server Sync Recipe")
            .assertExists()
        composeTestRule.onNodeWithText("Server Sync Recipe")
            .performClick()

        // Wait for edit screen to load with extended timeout
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("Delete recipe"), 30000)

        composeTestRule.onNodeWithContentDescription("Delete recipe")
            .assertExists()
        composeTestRule.onNodeWithContentDescription("Delete recipe")
            .performClick()

        // Wait for navigation back to recipe list with extended timeout
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("New recipe"), 30000)
        composeTestRule.waitForIdle()

        // Verify recipe is deleted locally
        assertNodeDoesNotExist("Server Sync Recipe", "Recipe should be deleted locally but still exists")

        // Login again with longer timeout for sync
        loginToBackend(45000)

        // Verify the recipe is still deleted (should not appear after sync)
        assertNodeDoesNotExist("Server Sync Recipe", "Recipe should remain deleted after sync but was found")
        assertNodeDoesNotExist("This recipe should sync to server", "Recipe content should remain deleted after sync but was found")
    }

    @Test
    fun createRecipe_ThenLoginToServer_VerifySynchronized() {
        // Ensure we start in a clean state
        composeTestRule.waitForIdle()
        
        // Wait for the new recipe button to be available
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("New recipe"), 20000)
        
        // Verify we're logged out initially
        composeTestRule.onNodeWithContentDescription("Synchronisation with the backend is disabled")
            .assertExists()

        // Create a recipe without being logged in
        createRecipe("Local Recipe", "This should sync when we login")

        // Verify the recipe was created
        composeTestRule.onNodeWithText("Local Recipe").assertExists()
        composeTestRule.onNodeWithText("This should sync when we login").assertExists()

        // Login to backend with longer timeout for sync
        loginToBackend(45000)

        // Verify we're now logged in
        composeTestRule.onNodeWithContentDescription("Synchronisation with the backend is enabled")
            .assertExists()

        // Verify the recipe is still there after login (it should have synced to server)
        composeTestRule.onNodeWithText("Local Recipe")
            .assertExists()
        composeTestRule.onNodeWithText("This should sync when we login")
            .assertExists()
    }

    private fun loginToBackend(timeout: Long = 40000) {
        // Ensure we start from a logged-out state
        composeTestRule.waitForIdle()
        
        // Click on sync settings
        composeTestRule.onNodeWithContentDescription("Synchronisation with the backend is disabled")
            .assertExists()
        composeTestRule.onNodeWithContentDescription("Synchronisation with the backend is disabled")
            .performClick()

        // Wait for login screen to load with extended timeout
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("Server URI"), 30000)
        composeTestRule.waitForIdle()

        // Clear and enter server URI with waits
        composeTestRule.onNodeWithContentDescription("Server URI")
            .assertExists()
            .performTextClearance()
        Thread.sleep(500)
        composeTestRule.onNodeWithContentDescription("Server URI")
            .performTextInput(FakeBackend.server)
        
        composeTestRule.waitForIdle()
        Thread.sleep(500)

        // Clear and enter username with waits
        composeTestRule.onNodeWithContentDescription("User name")
            .assertExists()
            .performTextClearance()
        Thread.sleep(500)
        composeTestRule.onNodeWithContentDescription("User name")
            .performTextInput(FakeBackend.testUser)
        
        composeTestRule.waitForIdle()
        Thread.sleep(500)

        // Clear and enter password with waits
        composeTestRule.onNodeWithContentDescription("Password")
            .assertExists()
            .performTextClearance()
        Thread.sleep(500)
        composeTestRule.onNodeWithContentDescription("Password")
            .performTextInput(FakeBackend.testPassword)

        composeTestRule.waitForIdle()
        Thread.sleep(500)

        // Click login button
        composeTestRule.onNodeWithContentDescription("Login")
            .assertExists()
        composeTestRule.onNodeWithContentDescription("Login")
            .performClick()

        // Wait for login to complete and return to recipe list with extended timeout
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("New recipe"), timeout)
        composeTestRule.waitForIdle()
    }
}
