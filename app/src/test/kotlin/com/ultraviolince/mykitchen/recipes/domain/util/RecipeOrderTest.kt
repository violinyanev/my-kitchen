package com.ultraviolince.mykitchen.recipes.domain.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RecipeOrderTest {

    @Test
    fun `Title order holds orderType correctly`() {
        val titleAsc = RecipeOrder.Title(OrderType.Ascending)
        val titleDesc = RecipeOrder.Title(OrderType.Descending)

        assertThat(titleAsc.orderType).isEqualTo(OrderType.Ascending)
        assertThat(titleDesc.orderType).isEqualTo(OrderType.Descending)
    }

    @Test
    fun `Date order holds orderType correctly`() {
        val dateAsc = RecipeOrder.Date(OrderType.Ascending)
        val dateDesc = RecipeOrder.Date(OrderType.Descending)

        assertThat(dateAsc.orderType).isEqualTo(OrderType.Ascending)
        assertThat(dateDesc.orderType).isEqualTo(OrderType.Descending)
    }

    @Test
    fun `copy creates new Title instance with different orderType`() {
        val original = RecipeOrder.Title(OrderType.Ascending)
        val copied = original.copy(OrderType.Descending)

        assertThat(copied).isInstanceOf(RecipeOrder.Title::class.java)
        assertThat(copied.orderType).isEqualTo(OrderType.Descending)
        assertThat(copied).isNotSameInstanceAs(original)
    }

    @Test
    fun `copy creates new Date instance with different orderType`() {
        val original = RecipeOrder.Date(OrderType.Ascending)
        val copied = original.copy(OrderType.Descending)

        assertThat(copied).isInstanceOf(RecipeOrder.Date::class.java)
        assertThat(copied.orderType).isEqualTo(OrderType.Descending)
        assertThat(copied).isNotSameInstanceAs(original)
    }

    @Test
    fun `copy preserves same orderType when provided`() {
        val original = RecipeOrder.Title(OrderType.Ascending)
        val copied = original.copy(OrderType.Ascending)

        assertThat(copied.orderType).isEqualTo(OrderType.Ascending)
        assertThat(copied.orderType).isEqualTo(original.orderType)
    }

    @Test
    fun `Title copy with Ascending to Descending`() {
        val original = RecipeOrder.Title(OrderType.Ascending)
        val copied = original.copy(OrderType.Descending)

        assertThat(copied).isInstanceOf(RecipeOrder.Title::class.java)
        assertThat(copied.orderType).isEqualTo(OrderType.Descending)
        assertThat(original.orderType).isEqualTo(OrderType.Ascending) // Original unchanged
    }

    @Test
    fun `Date copy with Descending to Ascending`() {
        val original = RecipeOrder.Date(OrderType.Descending)
        val copied = original.copy(OrderType.Ascending)

        assertThat(copied).isInstanceOf(RecipeOrder.Date::class.java)
        assertThat(copied.orderType).isEqualTo(OrderType.Ascending)
        assertThat(original.orderType).isEqualTo(OrderType.Descending) // Original unchanged
    }

    @Test
    fun `Title and Date orders are different types`() {
        val titleOrder = RecipeOrder.Title(OrderType.Ascending)
        val dateOrder = RecipeOrder.Date(OrderType.Ascending)

        assertThat(titleOrder::class).isNotEqualTo(dateOrder::class)
        assertThat(titleOrder).isNotEqualTo(dateOrder)
    }

    @Test
    fun `RecipeOrder inheritance works correctly`() {
        val titleOrder: RecipeOrder = RecipeOrder.Title(OrderType.Ascending)
        val dateOrder: RecipeOrder = RecipeOrder.Date(OrderType.Descending)

        assertThat(titleOrder).isInstanceOf(RecipeOrder::class.java)
        assertThat(dateOrder).isInstanceOf(RecipeOrder::class.java)
        assertThat(titleOrder.orderType).isEqualTo(OrderType.Ascending)
        assertThat(dateOrder.orderType).isEqualTo(OrderType.Descending)
    }

    @Test
    fun `copy functionality covers all when branches`() {
        // Test both when branches in the copy function
        val titleOriginal = RecipeOrder.Title(OrderType.Ascending)
        val titleCopied = titleOriginal.copy(OrderType.Descending)

        val dateOriginal = RecipeOrder.Date(OrderType.Ascending)
        val dateCopied = dateOriginal.copy(OrderType.Descending)

        // Verify Title branch
        assertThat(titleCopied).isInstanceOf(RecipeOrder.Title::class.java)
        assertThat(titleCopied.orderType).isEqualTo(OrderType.Descending)

        // Verify Date branch
        assertThat(dateCopied).isInstanceOf(RecipeOrder.Date::class.java)
        assertThat(dateCopied.orderType).isEqualTo(OrderType.Descending)
    }
}
