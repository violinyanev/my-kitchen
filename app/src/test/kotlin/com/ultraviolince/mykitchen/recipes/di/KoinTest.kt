package com.ultraviolince.mykitchen.recipes.di

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class HiltTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Test
    fun checkAllModules() {
        // Hilt automatically verifies all modules during compilation
        // This test ensures the Hilt setup works correctly
        hiltRule.inject()
        // If this test runs without errors, the DI setup is correct
    }
}
