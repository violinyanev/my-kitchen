package com.example.myapplication

import android.content.Context
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.myapplication.recipes.presentation.MainScreen
import com.example.myapplication.recipes.presentation.editrecipe.AddEditRecipeScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog


@RunWith(RobolectricTestRunner::class)
class EditRecipeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    @Throws(Exception::class)
    fun setUp() {
        ShadowLog.stream = System.out
    }

    @Test
    fun checkWithEmptyList() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        composeTestRule.setContent {
            MyApplicationTheme {
                MainScreen()
            }
        }

        composeTestRule.onNode(hasTestTag("list_item_0")).assertDoesNotExist()
        composeTestRule.onNode(hasTestTag("list_item_1")).assertDoesNotExist()
    }
}