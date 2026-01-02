package com.ultraviolince.mykitchen.macrobenchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Benchmarks for measuring scrolling performance and UI jank in recipe lists.
 * This will help identify performance issues when users interact with dynamic UI elements.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class ScrollingBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun scrollRecipeList() = benchmarkRule.measureRepeated(
        packageName = "com.ultraviolince.mykitchen",
        metrics = listOf(FrameTimingMetric()),
        iterations = 5,
        startupMode = StartupMode.WARM,
        compilationMode = CompilationMode.Partial(),
        setupBlock = {
            // Navigate to the app and ensure we're on a screen with scrollable content
            pressHome()
            startActivityAndWait()
            // Wait for the app to fully load
            device.wait(Until.hasObject(By.pkg(packageName)), 5_000)
        }
    ) {
        scrollRecipeListInternal()
    }

    @Test
    fun scrollRecipeListWithoutCompilation() = benchmarkRule.measureRepeated(
        packageName = "com.ultraviolince.mykitchen",
        metrics = listOf(FrameTimingMetric()),
        iterations = 5,
        startupMode = StartupMode.WARM,
        compilationMode = CompilationMode.None(),
        setupBlock = {
            pressHome()
            startActivityAndWait()
            device.wait(Until.hasObject(By.pkg(packageName)), 5_000)
        }
    ) {
        scrollRecipeListInternal()
    }

    @Test
    fun navigateAndScroll() = benchmarkRule.measureRepeated(
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
        navigateAndScrollInternal()
    }

    private fun MacrobenchmarkScope.scrollRecipeListInternal() {
        // Wait for any loading states to complete
        device.wait(Until.hasObject(By.pkg(packageName)), 3_000)
        
        // Look for scrollable elements - trying common Compose testTags and content descriptions
        val scrollableElement = device.findObject(By.scrollable(true)) 
            ?: device.findObject(By.pkg(packageName).clickable(true))
        
        if (scrollableElement != null) {
            // Perform scrolling gestures to measure frame timing
            repeat(5) {
                scrollableElement.scroll(Direction.DOWN, 1.0f)
                device.waitForIdle(100)
                scrollableElement.scroll(Direction.UP, 1.0f)
                device.waitForIdle(100)
            }
        } else {
            // Fallback: try scrolling the entire screen with fixed dimensions
            repeat(5) {
                device.swipe(500, 1200, 500, 400, 10)
                device.waitForIdle(100)
                device.swipe(500, 400, 500, 1200, 10)
                device.waitForIdle(100)
            }
        }
    }

    private fun MacrobenchmarkScope.navigateAndScrollInternal() {
        device.wait(Until.hasObject(By.pkg(packageName)), 3_000)
        
        // Look for navigation elements (bottom navigation, tabs, etc.)
        val navigationElement = device.findObject(By.clickable(true))
        navigationElement?.click()
        device.waitForIdle(500)
        
        // After navigation, perform scrolling
        scrollRecipeListInternal()
    }
}
