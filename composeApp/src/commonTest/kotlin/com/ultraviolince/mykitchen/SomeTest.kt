package com.ultraviolince.mykitchen

import assertk.assertThat
import assertk.assertions.isEqualTo
import getSomething
import kotlin.test.Test


class SomethingTest {
    @Test
    fun testGetInitials() {

        assertThat("Something").isEqualTo(getSomething())
    }
}