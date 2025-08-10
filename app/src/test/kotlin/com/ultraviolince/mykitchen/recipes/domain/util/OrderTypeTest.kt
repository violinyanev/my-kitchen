package com.ultraviolince.mykitchen.recipes.domain.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class OrderTypeTest {

    @Test
    fun `OrderType Ascending is singleton object`() {
        val ascending1 = OrderType.Ascending
        val ascending2 = OrderType.Ascending

        assertThat(ascending1).isSameInstanceAs(ascending2)
    }

    @Test
    fun `OrderType Descending is singleton object`() {
        val descending1 = OrderType.Descending
        val descending2 = OrderType.Descending

        assertThat(descending1).isSameInstanceAs(descending2)
    }

    @Test
    fun `Ascending and Descending are different instances`() {
        assertThat(OrderType.Ascending).isNotSameInstanceAs(OrderType.Descending)
    }
}
