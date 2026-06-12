package com.ultraviolince.mykitchen

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.net.HttpURLConnection
import java.net.URL

/**
 * Smoke tests that verify the core end-to-end flow against a real server.
 *
 * The server is expected to be running and reachable at http://10.0.2.2:5000
 * (the Android emulator's alias for the host machine).
 */
@OptIn(ExperimentalTestApi::class)
class SmokeTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun ensureTestUserExists() {
        // Register the test user before each run; 409 Conflict is expected if already registered.
        val thread = Thread {
            try {
                val url = URL("${FakeServer.URL}/users/register")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true
                conn.outputStream.writer().use { writer ->
                    writer.write("""{"email":"${FakeServer.EMAIL}","password":"${FakeServer.PASSWORD}"}""")
                }
                conn.responseCode // triggers the request; ignore the result
                conn.disconnect()
            } catch (_: Exception) {
                // Server not reachable — the test will fail at the login assertion step
            }
        }
        thread.start()
        thread.join(5_000)
    }

    @Test
    fun loginToServerThenCreateRecipe() {
        // Login screen — replace default localhost URL with the emulator host alias
        with(composeTestRule.onNodeWithTag("server_url_field")) {
            performTextClearance()
            performTextInput(FakeServer.URL)
        }
        composeTestRule.onNodeWithTag("email_field").performTextInput(FakeServer.EMAIL)
        composeTestRule.onNodeWithTag("password_field").performTextInput(FakeServer.PASSWORD)
        composeTestRule.onNodeWithTag("login_button").performClick()

        // Wait for the recipe list to appear (FAB signals we are on the list screen)
        composeTestRule.waitUntilExactlyOneExists(hasContentDescription("Add recipe"), 10_000)

        // Open the add-recipe screen
        composeTestRule.onNodeWithContentDescription("Add recipe").performClick()
        composeTestRule.waitForIdle()

        // Fill in the recipe
        composeTestRule.onNodeWithTag("title_field").performTextInput("Smoke Test Recipe")
        composeTestRule.onNodeWithTag("content_field").performTextInput("Created by instrumented smoke test")
        composeTestRule.onNodeWithTag("save_button").performClick()

        // Back on the recipe list — recipe title must be visible
        composeTestRule.waitUntilExactlyOneExists(hasText("Smoke Test Recipe"), 10_000)
        composeTestRule.onNodeWithText("Smoke Test Recipe").assertIsDisplayed()
    }

    private object FakeServer {
        // 10.0.2.2 is the Android emulator's alias for the host machine
        const val URL = "http://10.0.2.2:5000"
        const val EMAIL = "smoketest@mykitchen.test"
        const val PASSWORD = "SmokeTest123"
    }
}
