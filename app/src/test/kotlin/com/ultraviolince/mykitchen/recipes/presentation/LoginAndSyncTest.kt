package com.ultraviolince.mykitchen.recipes.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
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
class LoginAndSyncTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        ApplicationProvider.getApplicationContext<Application>()
        val server = GlobalContext.get().get<InMemoryRecipesServer>()
        server.reset()
        server.seed(
            BackendRecipe(id = 10, title = "Soup", body = "Carrot", timestamp = 10)
        )
    }

    @Test
    fun login_flow_enables_sync_and_loads_data() {
        val context = composeRule.activity

        composeRule.onRoot().captureRoboImage("login/01_recipes_logged_out.png")
        composeRule.onNodeWithContentDescription(context.getString(R.string.sync_disabled)).performClick()

        composeRule.onNodeWithContentDescription("Login").assertIsDisplayed()
        composeRule.onRoot().captureRoboImage("login/02_login.png")
        composeRule.onNodeWithContentDescription("Login").performClick()

        composeRule.onNodeWithContentDescription(context.getString(R.string.sync_enabled)).assertIsDisplayed()
        composeRule.onNodeWithText("Soup").assertIsDisplayed()
        composeRule.onRoot().captureRoboImage("login/03_recipes_logged_in.png")
    }
}

