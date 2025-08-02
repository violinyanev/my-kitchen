package com.ultraviolince.mykitchen.macrobenchmark

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Generates a baseline profile for the app, which can be used to optimize startup performance
 * and reduce jank. The baseline profile contains a list of classes and methods that should be
 * precompiled during app installation.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() = baselineProfileRule.collect(
        packageName = "com.ultraviolince.mykitchen"
    ) {
        // Start the app from cold state
        pressHome()
        startActivityAndWait()
        
        // Wait for the app to be fully loaded
        device.wait(Until.hasObject(By.pkg(packageName)), 5_000)
        
        // Navigate through the main user journeys to capture the critical path
        // This will include the most commonly used features
        
        // Let the app settle
        device.waitForIdle(2_000)
        
        // Scroll through main content to capture scrolling performance
        val scrollableElement = device.findObject(By.scrollable(true))
        if (scrollableElement != null) {
            repeat(3) {
                scrollableElement.scroll(Direction.DOWN, 0.8f)
                device.waitForIdle(500)
            }
            repeat(2) {
                scrollableElement.scroll(Direction.UP, 0.8f)
                device.waitForIdle(500)
            }
        } else {
            // Fallback scrolling
            val displayMetrics = device.displayMetrics
            val width = displayMetrics.widthPixels
            val height = displayMetrics.heightPixels
            
            repeat(3) {
                device.swipe(width / 2, height * 3 / 4, width / 2, height / 4, 10)
                device.waitForIdle(500)
            }
        }
        
        // Try to navigate to different screens if navigation is available
        val navigationElements = device.findObjects(By.clickable(true))
        navigationElements.take(3).forEach { element ->
            try {
                element.click()
                device.waitForIdle(1_000)
                
                // Briefly interact with the new screen
                device.findObject(By.scrollable(true))?.scroll(Direction.DOWN, 0.5f)
                device.waitForIdle(500)
                
            } catch (e: Exception) {
                // Continue with baseline profile generation
            }
        }
        
        // Try to interact with some common UI elements
        val clickableElements = device.findObjects(By.clickable(true))
        clickableElements.take(2).forEach { element ->
            try {
                element.click()
                device.waitForIdle(500)
                device.pressBack()
                device.waitForIdle(500)
            } catch (e: Exception) {
                // Continue
            }
        }
        
        // Return to home screen
        device.pressBack()
        device.waitForIdle(1_000)
    }
}