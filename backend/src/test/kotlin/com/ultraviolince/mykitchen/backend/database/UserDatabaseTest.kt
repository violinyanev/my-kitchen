package com.ultraviolince.mykitchen.backend.database

import com.ultraviolince.mykitchen.backend.model.User
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteIfExists

class UserDatabaseTest {
    private lateinit var testFile: Path
    private val testUser = User("Joe", "joe@example.com", "password123")

    @Before
    fun setUp() {
        testFile = Files.createTempFile("test_database", ".yaml")
    }

    @After
    fun tearDown() {
        testFile.deleteIfExists()
    }

    @Test
    fun testInit() {
        val db = UserDatabase(testFile)
        assertTrue(Files.exists(testFile))
        assertEquals(emptyList<User>(), db.getAll())
    }

    @Test
    fun testValidateLoginRequestSuccess() {
        val db = UserDatabase(testFile)
        db.create(testUser.email, testUser.name, testUser.password)

        val (user, error) = db.validateLoginRequest(testUser.email, testUser.password)
        assertNull(error)
        assertNotNull(user)
        assertEquals(testUser.email, user?.email)
        assertEquals(testUser.name, user?.name)
    }

    @Test
    fun testValidateLoginRequestBadCredentials() {
        val db = UserDatabase(testFile)
        db.create(testUser.email, testUser.name, testUser.password)

        val (user, error) = db.validateLoginRequest(testUser.email, "wrongpassword")
        assertNull(user)
        assertEquals("Bad credentials", error)
    }

    @Test
    fun testValidateLoginRequestMissingEmail() {
        val db = UserDatabase(testFile)

        val (user, error) = db.validateLoginRequest(null, testUser.password)
        assertNull(user)
        assertEquals("Must provide user email and password", error)
    }

    @Test
    fun testValidateLoginRequestUserNotFound() {
        val db = UserDatabase(testFile)

        val (user, error) = db.validateLoginRequest("nonexistent@example.com", testUser.password)
        assertNull(user)
        assertEquals("Could not find user with email nonexistent@example.com", error)
    }

    @Test
    fun testCreateUser() {
        val db = UserDatabase(testFile)
        val createdUser = db.create(testUser.email, testUser.name, testUser.password)

        assertEquals(testUser.email, createdUser.email)
        assertEquals(testUser.name, createdUser.name)
        assertEquals(testUser.password, createdUser.password)

        val allUsers = db.getAll()
        assertEquals(1, allUsers.size)
        assertEquals(createdUser, allUsers[0])
    }

    @Test
    fun testGetByUsername() {
        val db = UserDatabase(testFile)
        db.create(testUser.email, testUser.name, testUser.password)

        val foundUser = db.getByUsername(testUser.name)
        assertNotNull(foundUser)
        assertEquals(testUser.name, foundUser?.name)

        val notFoundUser = db.getByUsername("NonExistentUser")
        assertNull(notFoundUser)
    }
}
