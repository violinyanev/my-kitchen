package com.ultraviolince.mykitchen.recipes.data.datasource.localdb

import androidx.sqlite.db.SupportSQLiteDatabase
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test

class RecipeDatabaseTest {

    @Test
    fun `database name is correct`() {
        assertEquals("recipedb", RecipeDatabase.DATABASE_NAME)
    }

    @Test
    fun `migration1To2 has correct start and end version`() {
        val migration = RecipeDatabase.migration1To2
        assertEquals(1, migration.startVersion)
        assertEquals(2, migration.endVersion)
    }

    @Test
    fun `migration1To2 adds imagePath column to Recipe table`() {
        // Arrange
        val mockDatabase = mockk<SupportSQLiteDatabase>(relaxed = true)
        val migration = RecipeDatabase.migration1To2

        // Act
        migration.migrate(mockDatabase)

        // Assert
        verify { mockDatabase.execSQL("ALTER TABLE Recipe ADD COLUMN imagePath TEXT") }
    }
}