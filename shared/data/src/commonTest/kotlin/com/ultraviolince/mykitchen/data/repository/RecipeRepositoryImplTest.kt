package com.ultraviolince.mykitchen.data.repository

import com.ultraviolince.mykitchen.data.fake.FakeRecipeDao
import com.ultraviolince.mykitchen.data.fake.buildMockApiClient
import com.ultraviolince.mykitchen.data.remote.dto.RecipeDto
import com.ultraviolince.mykitchen.data.store.InMemoryCredentialsStore
import com.ultraviolince.mykitchen.domain.model.AuthState
import com.ultraviolince.mykitchen.domain.model.Recipe
import com.ultraviolince.mykitchen.domain.model.RecipeOrder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RecipeRepositoryImplTest {

    private fun buildRepo(
        loginToken: String = "test-token",
        loginSuccess: Boolean = true,
        remoteRecipes: List<RecipeDto> = emptyList(),
    ): Triple<RecipeRepositoryImpl, FakeRecipeDao, InMemoryCredentialsStore> {
        val dao = FakeRecipeDao()
        val api = buildMockApiClient(
            loginToken = loginToken,
            loginSuccess = loginSuccess,
            remoteRecipes = remoteRecipes,
        )
        val creds = InMemoryCredentialsStore()
        return Triple(RecipeRepositoryImpl(dao, api, creds), dao, creds)
    }

    @Test
    fun insertRecipeAndGetRecipeByIdRoundTrips() = runTest {
        val (repo) = buildRepo()
        val r = Recipe.create("Pasta", "Boil water")
        repo.insertRecipe(r)
        assertEquals(r.title, repo.getRecipeById(r.id)?.title)
    }

    @Test
    fun deleteRecipeSoftDeletesSoRecipeDisappearsFromGetRecipes() = runTest {
        val (repo) = buildRepo()
        val r = Recipe.create("Pasta", "Boil water")
        repo.insertRecipe(r)
        repo.deleteRecipe(r.id)
        val items = repo.getRecipes(RecipeOrder.Title()).first()
        assertTrue(items.isEmpty())
    }

    @Test
    fun getRecipeByIdReturnsNullForMissingId() = runTest {
        val (repo) = buildRepo()
        assertNull(repo.getRecipeById("no-such-id"))
    }

    @Test
    fun loginSetsLoggedInAuthState() = runTest {
        val (repo) = buildRepo(loginToken = "my-jwt")
        repo.login("user@test.com", "pass", "http://localhost:5000")
        assertTrue(repo.getAuthState().first() is AuthState.LoggedIn)
    }

    @Test
    fun loginFailureLeavesAuthStateAsLoggedOut() = runTest {
        val (repo) = buildRepo(loginSuccess = false)
        repo.login("user@test.com", "wrong", "http://localhost:5000")
        assertEquals(AuthState.LoggedOut, repo.getAuthState().first())
    }

    @Test
    fun logoutClearsAuthState() = runTest {
        val (repo) = buildRepo()
        repo.login("user@test.com", "pass", "http://localhost:5000")
        repo.logout()
        assertEquals(AuthState.LoggedOut, repo.getAuthState().first())
    }

    @Test
    fun syncRecipesPullsRemoteRecipesIntoLocalDb() = runTest {
        val remote = listOf(
            RecipeDto(id = "remote-1", title = "Remote Pasta", content = "Remote", createdAt = 1000L, updatedAt = 1000L),
        )
        val (repo) = buildRepo(remoteRecipes = remote)
        repo.login("user@test.com", "pass", "http://localhost:5000")
        repo.syncRecipes()
        val items = repo.getRecipes(RecipeOrder.Title()).first()
        assertEquals(1, items.size)
        assertEquals("Remote Pasta", items.first().title)
    }

    @Test
    fun syncRecipesReturnsFailureWhenNotLoggedIn() = runTest {
        val (repo) = buildRepo()
        val result = repo.syncRecipes()
        assertTrue(result.isFailure)
    }
}
