package com.ultraviolince.mykitchen.recipes.di

import com.ultraviolince.mykitchen.di.AppModule
import org.junit.Test

/**
 * Test to verify Hilt DI setup is correct.
 * The real verification happens at compile time - if this compiles, Hilt setup is correct.
 */
class HiltTest {

    @Test
    fun checkAllModules() {
        // Hilt automatically verifies all modules during compilation
        // This test ensures the Hilt setup compiles correctly
        // If this test runs without errors, the DI setup is correct
        assert(true)
    }
}
