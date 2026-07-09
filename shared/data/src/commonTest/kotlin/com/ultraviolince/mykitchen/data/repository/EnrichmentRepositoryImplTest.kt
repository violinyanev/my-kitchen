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
        beautifySucceeds: Boolean = true,
    ): EnrichmentRepositoryImpl {
        val api = buildMockEnrichmentApiClient(beautifySucceeds = beautifySucceeds)
        val creds = InMemoryCredentialsStore()
        if (loggedIn) {
            creds.saveCredentials("tok", "http://localhost:5000")
        }
        return EnrichmentRepositoryImpl(api, creds)
    }

    @Test
    fun beautifyReturnsEnrichmentWhenLoggedIn() = runTest {
        val repo = buildRepo()
        val result = repo.beautify("recipe-1")
        assertTrue(result.isSuccess)
        assertEquals("Tasty", result.getOrNull()?.summary)
    }

    @Test
    fun beautifyFailsWhenNotLoggedIn() = runTest {
        val repo = buildRepo(loggedIn = false)
        val result = repo.beautify("recipe-1")
        assertTrue(result.isFailure)
    }

    @Test
    fun beautifyPropagatesApiFailure() = runTest {
        val repo = buildRepo(beautifySucceeds = false)
        val result = repo.beautify("recipe-1")
        assertTrue(result.isFailure)
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
    fun refineReturnsUpdatedEnrichmentWhenLoggedIn() = runTest {
        val repo = buildRepo()
        val result = repo.refine("recipe-1", "spicier please")
        assertTrue(result.isSuccess)
    }

    @Test
    fun refineFailsWhenNotLoggedIn() = runTest {
        val repo = buildRepo(loggedIn = false)
        val result = repo.refine("recipe-1", "feedback")
        assertTrue(result.isFailure)
    }

    @Test
    fun deleteEnrichmentSucceedsWhenLoggedIn() = runTest {
        val repo = buildRepo()
        val result = repo.deleteEnrichment("recipe-1")
        assertTrue(result.isSuccess)
    }

    @Test
    fun deleteEnrichmentFailsWhenNotLoggedIn() = runTest {
        val repo = buildRepo(loggedIn = false)
        val result = repo.deleteEnrichment("recipe-1")
        assertTrue(result.isFailure)
    }
}
