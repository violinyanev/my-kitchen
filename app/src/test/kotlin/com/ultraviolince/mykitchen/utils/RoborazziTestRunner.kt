package com.ultraviolince.mykitchen.utils

import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.ultraviolince.mykitchen.TestApplication

@Config(application = TestApplication::class)
@Suppress("unused")
class RoborazziTestRunner(testClass: Class<*>) : RobolectricTestRunner(testClass)
