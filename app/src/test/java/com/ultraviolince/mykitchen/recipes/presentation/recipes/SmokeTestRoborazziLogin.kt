package com.ultraviolince.mykitchen.recipes.presentation.recipes

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.ultraviolince.mykitchen.recipes.data.FakeBackend
import com.ultraviolince.mykitchen.recipes.presentation.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = com.ultraviolince.mykitchen.TestApplication::class)
class SmokeTestRoborazziLogin {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        // No specific setup needed for Robolectric tests
        // WorkManager will be mocked through the TestApplication
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

    @Test
    fun loginToBackendThenCreateRecipe() {
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
}
