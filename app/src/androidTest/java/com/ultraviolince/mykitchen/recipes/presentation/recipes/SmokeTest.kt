package com.ultraviolince.mykitchen.recipes.presentation.recipes

import android.util.Log
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.RecipeService
import com.ultraviolince.mykitchen.recipes.presentation.MainActivity
import com.ultraviolince.mykitchen.recipes.utils.FileReader
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
class SmokeTest {

    private val mockWebServer = MockWebServer()
    private lateinit var mockServerUrl: String
    private val testUser = "TestUser"
    private val testPassword = "TestPassword"
    private val token = "Token"


    private val hiltRule = HiltAndroidRule(this)
    private val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val rule: RuleChain = RuleChain
        .outerRule(hiltRule)
        .around(composeTestRule)

    private fun fakeServerLogin() {
        mockWebServer.enqueue(
            MockResponse().setBody("{\"data\":{\"email\":\"test@user.com\",\"token\":\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6IlRlc3RVc2VyIn0.kH9ebqYvk7Jo_DWYJfc392BfUf8OZBFPqw8x9KD-5O0\",\"username\":\"TestUser\"},\"message\":\"Successfully created authentication token\"}\n")

            //FileReader.readStringFromFile("login_response.json"))
        )
    }

    @Before
    fun setup() {
        println("############# SETUP BEGIN ################")
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()

        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
        mockWebServer.start()
        runBlocking {
            mockServerUrl = mockWebServer.url("/").toString()
        }
        println("############# SETUP END ################")
    }

    @After
    fun teardown() {
        println("############# TEARDOWN BEGIN ################")
        mockWebServer.shutdown()
        println("############# TEARDOWN END ################")
    }

    private fun createRecipe(title: String, content: String) {
        println("############# CREATE RECIPE ################")
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
        println("############# RECIPE CREATED ################")
    }

    @Test fun createRecipe_WithoutLogin() {
        // By default, no cloud sync
        with(composeTestRule.onNodeWithContentDescription("Synchronisation with the backend is disabled")) {
            assertExists()
            assertIsDisplayed()
        }

        createRecipe("recipe1", "content1")
    }

    // TODO Fix the test
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
            performTextInput(mockServerUrl)
        }
        with(composeTestRule.onNodeWithContentDescription("User name")) {
            assertExists()
            assertIsDisplayed()
            performTextInput(testUser)
        }
        with(composeTestRule.onNodeWithContentDescription("Password")) {
            assertExists()
            assertIsDisplayed()
            performTextInput(testPassword)
        }

        fakeServerLogin()

        // Login
        with(composeTestRule.onNodeWithContentDescription("Login")) {
            assertExists()
            assertIsDisplayed()
            performClick()
        }

        // Create a new recipe, with backend now
        //composeTestRule.waitUntilExactlyOneExists(hasContentDescription("New recipe"), 5000)*/

        //createRecipe("recipe2", "content2")
    }
}
