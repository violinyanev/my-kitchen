package com.ultraviolince.mykitchen.recipes.data.service

import com.ultraviolince.mykitchen.recipes.data.datasource.datastore.SafeDataStore
import com.ultraviolince.mykitchen.recipes.data.datasource.datastore.UserPreferences
import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
import com.ultraviolince.mykitchen.recipes.domain.service.NetworkService
import io.ktor.client.HttpClient
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AuthServiceImplTest {

    private lateinit var authService: AuthServiceImpl
    private lateinit var mockDataStore: SafeDataStore
    private lateinit var mockNetworkService: NetworkService
    private lateinit var mockHttpClient: HttpClient

    @Before
    fun setup() {
        mockDataStore = mockk(relaxed = true)
        mockNetworkService = mockk(relaxed = true)
        mockHttpClient = mockk(relaxed = true)

        every { mockNetworkService.createHttpClient(any(), any()) } returns mockHttpClient
        
        // Mock default empty preferences
        val emptyPreferences = UserPreferences(server = null, token = null)
        every { mockDataStore.preferences } returns MutableStateFlow(emptyPreferences)
        
        authService = AuthServiceImpl(mockDataStore, mockNetworkService)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `authService should be created successfully`() {
        assertNotNull(authService)
    }

    @Test
    fun `getLoginState returns initial empty state`() = runTest {
        val loginState = authService.getLoginState().first()
        assertEquals(LoginState.LoginEmpty, loginState)
    }

    @Test
    fun `init with valid credentials calls network service`() {
        // Given
        val storedPreferences = UserPreferences(server = "https://test.com", token = "test-token")
        every { mockDataStore.preferences } returns MutableStateFlow(storedPreferences)
        
        // When
        AuthServiceImpl(mockDataStore, mockNetworkService)
        
        // Then
        verify { mockNetworkService.createHttpClient("https://test.com", "test-token") }
    }

    @Test
    fun `logout calls datastore write`() = runTest {
        // When
        authService.logout()
        
        // Then
        coVerify { mockDataStore.write("", "") }
    }

    @Test
    fun `getLoginState returns flow`() {
        // When
        val loginStateFlow = authService.getLoginState()
        
        // Then
        assertNotNull(loginStateFlow)
    }
}