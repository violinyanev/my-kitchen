package com.ultraviolince.mykitchen.recipes.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import com.github.takahirom.roborazzi.captureRoboImage
import com.ultraviolince.mykitchen.R
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext
import org.robolectric.annotation.Config
import com.ultraviolince.mykitchen.ui.theme.MyApplicationTheme

@RunWith(com.ultraviolince.mykitchen.utils.RoborazziTestRunner::class)
@Config(sdk = [34])
class SortUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        try { GlobalContext.stopKoin() } catch (_: Exception) {}
    }

    @Test
    fun sort_section_toggles_and_captures() {
        val activity = composeRule.activity

        // Open sort section
        composeRule.onNodeWithContentDescription(activity.getString(R.string.sort)).performClick()
        composeRule.onNodeWithText(activity.getString(R.string.sort_by_heading)).assertIsDisplayed()
        composeRule.onRoot().captureRoboImage("sort/01_open.png")

        // Tap ascending
        composeRule.onNodeWithText("Ascending").performClick()
        composeRule.onRoot().captureRoboImage("sort/02_selected_ascending.png")

        // Close sort section
        composeRule.onNodeWithContentDescription(activity.getString(R.string.sort)).performClick()
        composeRule.onRoot().captureRoboImage("sort/03_closed.png")
    }
}

