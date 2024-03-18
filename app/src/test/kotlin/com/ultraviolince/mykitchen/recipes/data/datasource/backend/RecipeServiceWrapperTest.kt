package com.ultraviolince.mykitchen.recipes.data.datasource.backend

import com.ultraviolince.mykitchen.R
import com.ultraviolince.mykitchen.recipes.domain.repository.CloudSyncState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RecipeServiceWrapperTest {

    private val service = RecipeServiceWrapper()

    @Test
    fun `logs in to server successfully`() = runTest {
        assertEquals(service.login(FakeBackend.server, email = FakeBackend.testUser, FakeBackend.testPassword), CloudSyncState.SyncInProgress)
    }

    @Test
    fun `fails to log in to backend when the server url is wrong`() = runTest {
        val result = service.login("not valid address", email = FakeBackend.testUser, FakeBackend.testPassword)
        assertEquals(result, CloudSyncState.LoginFailure(R.string.malformed_server_uri))
    }

    @Test
    fun `fails to log in to backend when password is wrong`() = runTest {
        val result = service.login(FakeBackend.server, email = FakeBackend.testUser, "bad password")
        assertEquals(result, CloudSyncState.LoginFailure(R.string.wrong_credentials))
    }
}
