package com.example.myapplication.recipes.data.datasource.localdb


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MyRoomDatabaseUnitTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var myRoomDatabase: RecipeDatabase
    private lateinit var myDao: RecipeDao

    @Before
    fun setup() {
        myDao = mockk()
        myRoomDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RecipeDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDb() {
        myRoomDatabase.close()
    }

    @Test
    fun insertAndGetItem() = runBlocking {
        val item = MyItem(1, "Test Item")

        every { myDao.getById(1) } returns item

        val result = myDao.getById(1)

        assert(result == item)
    }

    // Write other test cases for update, delete, query, etc.
}


