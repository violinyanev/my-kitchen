package com.example.myapplication

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.myapplication.recipes.presentation.recipes.components.DefaultRadioButton
import com.example.myapplication.ui.theme.MyApplicationTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class RadioButtonTest {
    private companion object {
        private const val RADIOBUTTON_TAG = "radiobutton"
    }

    @get:Rule
    val rule: ComposeContentTestRule = createComposeRule()

    @Test
    fun testRadioButton() {
        val checkedState = mutableStateOf(false)

        rule.setContent {
            val checked = remember { checkedState }
            MyApplicationTheme {
                DefaultRadioButton(
                    selected = checked.value,
                    onSelect = { checked.value = true },
                    modifier = Modifier.testTag(RADIOBUTTON_TAG),
                    text = "test"
                )
            }
        }

        rule.onNodeWithTag(RADIOBUTTON_TAG).performClick()
        assertTrue(checkedState.value)

        rule.onNodeWithTag(RADIOBUTTON_TAG).performClick()
        assertTrue(checkedState.value)
    }
}
