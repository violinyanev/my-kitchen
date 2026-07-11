package com.ultraviolince.mykitchen.data.repository

import com.ultraviolince.mykitchen.data.fake.buildMockEnrichmentApiClient
import com.ultraviolince.mykitchen.data.store.InMemoryCredentialsStore
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EnrichmentRepositoryImplTest {

    private suspend fun buildRepo(
        loggedIn: Boolean = true,
        apiSucceeds: Boolean = true,
    ): EnrichmentRepositoryImpl {
        val api = buildMockEnrichmentApiClient(succeeds = apiSucceeds)
        val creds = InMemoryCredentialsStore()
        if (loggedIn) {
            creds.saveCredentials("tok", "http://localhost:5000")
        }
        return EnrichmentRepositoryImpl(api, creds)
    }

    @Test
    fun getEnrichmentReturnsEnrichmentWhenLoggedIn() = runTest {
        val repo = buildRepo()
        val result = repo.getEnrichment("recipe-1")
        assertTrue(result.isSuccess)
        assertEquals("enr-1", result.getOrNull()?.id)
    }

    @Test
    fun getEnrichmentFailsWhenNotLoggedIn() = runTest {
        val repo = buildRepo(loggedIn = false)
        val result = repo.getEnrichment("recipe-1")
        assertTrue(result.isFailure)
    }

    @Test
    fun getEnrichmentPropagatesApiFailure() = runTest {
        val repo = buildRepo(apiSucceeds = false)
        val result = repo.getEnrichment("recipe-1")
        assertTrue(result.isFailure)
    }

    @Test
    fun getEnrichmentsReturnsListWhenLoggedIn() = runTest {
        val repo = buildRepo()
        val result = repo.getEnrichments()
        assertTrue(result.isSuccess)
        assertEquals(listOf("enr-1"), result.getOrNull()?.map { it.id })
    }

    @Test
    fun getEnrichmentsFailsWhenNotLoggedIn() = runTest {
        val repo = buildRepo(loggedIn = false)
        val result = repo.getEnrichments()
        assertTrue(result.isFailure)
    }
}
