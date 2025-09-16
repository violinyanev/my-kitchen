package com.ultraviolince.mykitchen.backend.routes

import com.ultraviolince.mykitchen.backend.auth.AuthenticationService
import com.ultraviolince.mykitchen.backend.auth.JwtConfig
import com.ultraviolince.mykitchen.backend.database.UserDatabase
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.testing.testApplication
import io.ktor.serialization.kotlinx.json.json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteIfExists

class UserRoutesTest {
    private lateinit var testFile: Path
    private lateinit var userDatabase: UserDatabase
    private lateinit var jwtConfig: JwtConfig
    private lateinit var authService: AuthenticationService

    @Before
    fun setUp() {
        testFile = Files.createTempFile("test_database", ".yaml")
        userDatabase = UserDatabase(testFile)
        userDatabase.create("test@user.com", "TestUser", "TestPassword")

        jwtConfig = JwtConfig("test-secret-key")
        authService = AuthenticationService(jwtConfig, userDatabase)
    }

    @After
    fun tearDown() {
        testFile.deleteIfExists()
    }

    @Test
    fun testLoginSuccess() = testApplication {
        application {
            install(ContentNegotiation) { json() }
            configureUserRoutes(userDatabase, jwtConfig, authService)
        }

        val response = client.post("/users/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"test@user.com","password":"TestPassword"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val responseText = response.bodyAsText()
        assertTrue(responseText.contains("Successfully created authentication token"))
        assertTrue(responseText.contains("TestUser"))
        assertTrue(responseText.contains("test@user.com"))
    }

    @Test
    fun testGetUsersWithAuth() = testApplication {
        application {
            install(ContentNegotiation) { json() }
            configureUserRoutes(userDatabase, jwtConfig, authService)
        }

        val token = jwtConfig.generateToken("TestUser")
        val response = client.get("/users") {
            header("Authorization", "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val responseText = response.bodyAsText()
        assertTrue(responseText.contains("TestUser"))
        assertTrue(responseText.contains("test@user.com"))
    }

    @Test
    fun testGetUsersWithoutAuth() = testApplication {
        application {
            install(ContentNegotiation) { json() }
            configureUserRoutes(userDatabase, jwtConfig, authService)
        }

        val response = client.get("/users")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        val responseText = response.bodyAsText()
        assertTrue(responseText.contains("Authentication Token is missing"))
    }
}
