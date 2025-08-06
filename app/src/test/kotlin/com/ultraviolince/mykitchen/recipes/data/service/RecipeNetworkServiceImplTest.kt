package com.ultraviolince.mykitchen.recipes.data.service

import com.ultraviolince.mykitchen.recipes.data.datasource.datastore.SafeDataStore
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

    private val dataStore = mockk<SafeDataStore>()
    private val dao = mockk<RecipeDao>()
    private val networkService = mockk<NetworkService>()
    private val httpClient = mockk<HttpClient>()

    private val recipeNetworkService: RecipeNetworkService = RecipeNetworkServiceImpl(
        dataStore, dao, networkService
    )

    @Test
    fun `insertRecipe should handle no service gracefully`() = runTest {
        // Given
        every { dataStore.preferences } returns flowOf(mockk {
            every { server } returns null
            every { token } returns null
        })

        // When
        val result = recipeNetworkService.insertRecipe(1L, mockk())

        // Then
        assert(!result) // Should return false when no service available
    }

    @Test
    fun `deleteRecipe should handle no service gracefully`() = runTest {
        // Given
        every { dataStore.preferences } returns flowOf(mockk {
            every { server } returns null
            every { token } returns null
        })

        // When
        val result = recipeNetworkService.deleteRecipe(1L)

        // Then
        assert(!result) // Should return false when no service available
    }

    @Test
    fun `syncRecipes should handle no service gracefully`() = runTest {
        // Given
        every { dataStore.preferences } returns flowOf(mockk {
            every { server } returns null
            every { token } returns null
        })

        // When
        recipeNetworkService.syncRecipes()

        // Then
        // Should complete without errors even when no service available
    }
}