package com.ultraviolince.mykitchen.macrobenchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Benchmarks for measuring performance during user interactions like tapping buttons,
 * form filling, and navigation between screens.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class InteractionBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun tapInteractions() = benchmarkRule.measureRepeated(
        packageName = "com.ultraviolince.mykitchen",
        metrics = listOf(FrameTimingMetric()),
        iterations = 5,
        startupMode = StartupMode.WARM,
        compilationMode = CompilationMode.Partial(),
        setupBlock = {
            pressHome()
            startActivityAndWait()
            device.wait(Until.hasObject(By.pkg(packageName)), 5_000)
        }
    ) {
        performTapInteractions()
    }

    @Test
    fun navigationBenchmark() = benchmarkRule.measureRepeated(
        packageName = "com.ultraviolince.mykitchen",
        metrics = listOf(FrameTimingMetric(), StartupTimingMetric()),
        iterations = 5,
        startupMode = StartupMode.WARM,
        compilationMode = CompilationMode.Partial(),
        setupBlock = {
            pressHome()
            startActivityAndWait()
            device.wait(Until.hasObject(By.pkg(packageName)), 5_000)
        }
    ) {
        performNavigationSequence()
    }

    @Test
    fun formInteractionBenchmark() = benchmarkRule.measureRepeated(
        packageName = "com.ultraviolince.mykitchen",
        metrics = listOf(FrameTimingMetric()),
        iterations = 3, // Fewer iterations for complex interactions
        startupMode = StartupMode.WARM,
        compilationMode = CompilationMode.Partial(),
        setupBlock = {
            pressHome()
            startActivityAndWait()
            device.wait(Until.hasObject(By.pkg(packageName)), 5_000)
        }
    ) {
        performFormInteractions()
    }

    private fun MacrobenchmarkScope.performTapInteractions() {
        device.wait(Until.hasObject(By.pkg(packageName)), 3_000)
        
        // Find and tap interactive elements
        val clickableElements = device.findObjects(By.clickable(true))
        
        // Perform a series of taps on different elements
        clickableElements.take(3).forEach { element ->
            try {
                element.click()
                device.waitForIdle(300)
                
                // If a back button or close action is available, use it
                val backButton = device.findObject(By.desc("Navigate up")) 
                    ?: device.findObject(By.desc("Back"))
                backButton?.click() ?: device.pressBack()
                device.waitForIdle(300)
            } catch (e: Exception) {
                // Continue with next element if this one fails
                device.pressBack()
                device.waitForIdle(300)
            }
        }
    }

    private fun MacrobenchmarkScope.performNavigationSequence() {
        device.wait(Until.hasObject(By.pkg(packageName)), 3_000)
        
        // Look for bottom navigation or tabs
        val navigationItems = device.findObjects(By.clickable(true))
        
        // Navigate through different screens
        navigationItems.take(4).forEach { navItem ->
            try {
                navItem.click()
                device.waitForIdle(500)
                
                // Wait for content to load
                device.wait(Until.hasObject(By.pkg(packageName)), 2_000)
            } catch (e: Exception) {
                // Continue with navigation
            }
        }
    }

    private fun MacrobenchmarkScope.performFormInteractions() {
        device.wait(Until.hasObject(By.pkg(packageName)), 3_000)
        
        // Look for text input fields
        val textFields = device.findObjects(By.clazz("android.widget.EditText"))
        
        if (textFields.isNotEmpty()) {
            textFields.take(2).forEach { textField ->
                try {
                    textField.click()
                    device.waitForIdle(200)
                    textField.text = "Test input"
                    device.waitForIdle(200)
                } catch (e: Exception) {
                    // Continue with next field
                }
            }
            
            // Look for submit/save buttons
            val submitButton = device.findObject(By.text("Save")) 
                ?: device.findObject(By.text("Submit"))
                ?: device.findObject(By.text("Add"))
            
            submitButton?.click()
            device.waitForIdle(500)
        } else {
            // If no form fields, look for floating action buttons or add buttons
            val fabButton = device.findObject(By.desc("Add")) 
                ?: device.findObject(By.clazz("android.widget.ImageButton"))
            
            fabButton?.click()
            device.waitForIdle(500)
            
            // Try to find and interact with any modal or dialog that appears
            val dialogElements = device.findObjects(By.clickable(true))
            dialogElements.take(2).forEach { element ->
                try {
                    element.click()
                    device.waitForIdle(300)
                } catch (e: Exception) {
                    // Continue
                }
            }
        }
    }
}