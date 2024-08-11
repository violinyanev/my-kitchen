@file:OptIn(ExperimentalTestApi::class)

package com.ultraviolince.mykitchen

import AppContent
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test

class SampleUiTest {

    @Test
    fun testCounterCountsUp() = runComposeUiTest {
        setContent {
            AppContent()
        }

        onNodeWithText("Something").assertExists()
    }
}
