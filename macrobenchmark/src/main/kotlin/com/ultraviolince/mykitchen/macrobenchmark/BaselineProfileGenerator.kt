package com.ultraviolince.mykitchen.macrobenchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() = baselineProfileRule.collect(
        packageName = "com.ultraviolince.mykitchen"
    ) {
        pressHome()
        startActivityAndWait()

        device.wait(Until.hasObject(By.pkg(packageName)), 5_000)
        device.waitForIdle(2_000)

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
            repeat(3) {
                device.swipe(500, 1200, 500, 400, 10)
                device.waitForIdle(500)
            }
        }

        val navigationElements = device.findObjects(By.clickable(true))
        navigationElements.take(3).forEach { element ->
            try {
                element.click()
                device.waitForIdle(1_000)
                device.findObject(By.scrollable(true))?.scroll(Direction.DOWN, 0.5f)
                device.waitForIdle(500)
            } catch (e: Exception) {
                // Continue with baseline profile generation
            }
        }

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

        device.pressBack()
        device.waitForIdle(1_000)
    }
}
