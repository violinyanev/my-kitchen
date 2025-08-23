package com.ultraviolince.mykitchen.backend.auth

import com.ultraviolince.mykitchen.backend.database.UserDatabase
import com.ultraviolince.mykitchen.backend.model.User
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteIfExists

class AuthenticationTest {
    private lateinit var testFile: Path
    private lateinit var userDatabase: UserDatabase
    private lateinit var jwtConfig: JwtConfig
    private lateinit var authService: AuthenticationService
    private val testUser = User("TestUser", "test@example.com", "password123")

    @Before
    fun setUp() {
        testFile = Files.createTempFile("test_database", ".yaml")
        userDatabase = UserDatabase(testFile)
        userDatabase.create(testUser.email, testUser.name, testUser.password)
        
        jwtConfig = JwtConfig("test-secret-key")
        authService = AuthenticationService(jwtConfig, userDatabase)
    }

    @After
    fun tearDown() {
        testFile.deleteIfExists()
    }

    @Test
    fun testGenerateAndVerifyToken() {
        val token = jwtConfig.generateToken(testUser.name)
        assertNotNull(token)
        assertTrue(token.isNotEmpty())
        
        val username = jwtConfig.verifyToken(token)
        assertEquals(testUser.name, username)
    }

    @Test
    fun testVerifyInvalidToken() {
        val username = jwtConfig.verifyToken("invalid.token.here")
        assertNull(username)
    }

    @Test
    fun testAuthenticateValidToken() {
        val token = jwtConfig.generateToken(testUser.name)
        val user = authService.authenticate(token)
        
        assertNotNull(user)
        assertEquals(testUser.name, user?.name)
        assertEquals(testUser.email, user?.email)
    }

    @Test
    fun testAuthenticateInvalidToken() {
        val user = authService.authenticate("invalid.token.here")
        assertNull(user)
    }

    @Test
    fun testAuthenticateNullToken() {
        val user = authService.authenticate(null)
        assertNull(user)
    }

    @Test
    fun testAuthenticateTokenForNonExistentUser() {
        val token = jwtConfig.generateToken("NonExistentUser")
        val user = authService.authenticate(token)
        assertNull(user)
    }
}