package com.ultraviolince.mykitchen.recipes.data.service

import com.ultraviolince.mykitchen.recipes.data.datasource.datastore.SafeDataStore
import com.ultraviolince.mykitchen.recipes.data.datasource.datastore.UserPreferences
import com.ultraviolince.mykitchen.recipes.data.datasource.localdb.RecipeDao
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.service.NetworkService
import com.ultraviolince.mykitchen.recipes.domain.service.RecipeNetworkService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RecipeNetworkServiceImplTest {

    private val dataStore = mockk<SafeDataStore>(relaxed = true)
    private val dao = mockk<RecipeDao>(relaxed = true)
    private val networkService = mockk<NetworkService>(relaxed = true)
    private val httpClient = mockk<HttpClient>(relaxed = true)

    private fun createServiceWithMockedPrefs(server: String?, token: String?): RecipeNetworkService {
        every { dataStore.preferences } returns flowOf(
            UserPreferences(server = server, token = token)
        )
        return RecipeNetworkServiceImpl(dataStore, dao, networkService)
    }

    @Test
    fun `insertRecipe should handle no service gracefully`() = runTest {
        // Given
        val recipeNetworkService = createServiceWithMockedPrefs(server = null, token = null)

        // When
        val result = recipeNetworkService.insertRecipe(1L, mockk())

        // Then
        assert(!result) // Should return false when no service available
    }

    @Test
    fun `deleteRecipe should handle no service gracefully`() = runTest {
        // Given
        val recipeNetworkService = createServiceWithMockedPrefs(server = null, token = null)

        // When
        val result = recipeNetworkService.deleteRecipe(1L)

        // Then
        assert(!result) // Should return false when no service available
    }

    @Test
    fun `syncRecipes should handle no service gracefully`() = runTest {
        // Given
        val recipeNetworkService = createServiceWithMockedPrefs(server = null, token = null)

        // When
        recipeNetworkService.syncRecipes()

        // Then
        // Should complete without errors even when no service available
    }
}