package com.ultraviolince.mykitchen.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RecipeTest {
    @Test
    fun `create sets non-blank id`() {
        val r = Recipe.create("Pasta", "Cook it")
        assertTrue(r.id.isNotBlank())
        assertEquals("Pasta", r.title)
        assertEquals("Cook it", r.content)
        assertFalse(r.synced)
        assertFalse(r.deleted)
        assertTrue(r.timestamp > 0L)
    }

    @Test
    fun `two creates have different ids`() {
        val r1 = Recipe.create("A", "a")
        val r2 = Recipe.create("B", "b")
        assertTrue(r1.id != r2.id)
    }
}
