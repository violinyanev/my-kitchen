package com.example.myapplication.recipes.presentation

import android.content.res.Configuration
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.printToLog
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.test.platform.app.InstrumentationRegistry
import com.example.myapplication.di.AppModule
import com.example.myapplication.recipes.core.util.TestTags
import com.example.myapplication.recipes.core.util.TestTags.CONTENT_TEXT_FIELD
import com.example.myapplication.recipes.core.util.TestTags.TITLE_TEXT_FIELD
import com.example.myapplication.recipes.presentation.editrecipe.AddEditRecipeScreen
import com.example.myapplication.recipes.presentation.recipes.RecipeScreen
import com.example.myapplication.recipes.presentation.util.Screen
import com.example.myapplication.ui.theme.MyApplicationTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain


@HiltAndroidTest
class MainActivityTest {

    private val hiltRule = HiltAndroidRule(this)
    private val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val rule: RuleChain = RuleChain
        .outerRule(hiltRule)
        .around(composeTestRule)

    @Test fun clickAddRecipe_OpensNewRecipeScreen() {
        composeTestRule.onRoot().printToLog("TEST")

        // When the "Add" button is clicked
        with(composeTestRule.onNodeWithText("Add recipe")) {
            assertExists()
            assertIsDisplayed()
            performClick()
        }

        composeTestRule.waitForIdle()

        with(composeTestRule.onNodeWithText(TITLE_TEXT_FIELD)) {
            assertExists()
            assertIsDisplayed()
        }
        with(composeTestRule.onNodeWithText(CONTENT_TEXT_FIELD)) {
            assertExists()
            assertIsDisplayed()
        }
    }
}
