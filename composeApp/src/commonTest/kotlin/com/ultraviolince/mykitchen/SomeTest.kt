package com.ultraviolince.mykitchen

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class SomeTest {
    @Test
    fun testGetInitials() {
        assertThat("Something").isEqualTo("Something")
    }
}
