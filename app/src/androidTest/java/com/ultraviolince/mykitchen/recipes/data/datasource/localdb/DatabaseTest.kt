package com.ultraviolince.mykitchen.recipes.data.datasource.localdb

import androidx.room.migration.AutoMigrationSpec
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

// TODO enable this test

@Suppress("DEPRECATION")
@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {
    private val testDbName = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        RecipeDatabase::class.java.canonicalName!!,
        FrameworkSQLiteOpenHelperFactory()
    )

    /*@Test
    @Throws(IOException::class)
    fun migrate1To2() {
        helper.createDatabase(testDbName, 1).apply {
            // Database has schema version 1. Insert some data using SQL queries.
            // You can't use DAO classes because they expect the latest schema.
            execSQL("INSERT INTO Recipe (id, title, content, timestamp) VALUES (1, 'Recipe 1', 'Recipe Content 1', 123);")

            // Prepare for the next version.
            close()
        }

        helper.runMigrationsAndValidate(testDbName, 2, true)
    }*/
}
