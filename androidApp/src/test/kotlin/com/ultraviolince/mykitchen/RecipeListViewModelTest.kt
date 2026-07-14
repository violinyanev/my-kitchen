package com.ultraviolince.mykitchen

import com.ultraviolince.mykitchen.domain.model.AuthState
import com.ultraviolince.mykitchen.domain.model.Recipe
import com.ultraviolince.mykitchen.domain.model.RecipeEnrichment
import com.ultraviolince.mykitchen.domain.model.RecipeOrder
import com.ultraviolince.mykitchen.domain.model.SessionExpiredException
import com.ultraviolince.mykitchen.domain.model.User
import com.ultraviolince.mykitchen.domain.repository.EnrichmentRepository
import com.ultraviolince.mykitchen.domain.repository.RecipeRepository
import com.ultraviolince.mykitchen.domain.usecase.DeleteRecipeUseCase
import com.ultraviolince.mykitchen.domain.usecase.GetAuthStateUseCase
import com.ultraviolince.mykitchen.domain.usecase.GetEnrichmentsUseCase
import com.ultraviolince.mykitchen.domain.usecase.GetRecipesUseCase
import com.ultraviolince.mykitchen.domain.usecase.LogoutUseCase
import com.ultraviolince.mykitchen.domain.usecase.SyncRecipesUseCase
import com.ultraviolince.mykitchen.ui.screens.recipelist.RecipeListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(ScreenshotTestRunner::class)
class RecipeListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(
        syncResult: Result<Unit> = Result.success(Unit),
    ): Pair<RecipeListViewModel, FakeRecipeRepo> {
        val repo = FakeRecipeRepo(syncResult = syncResult)
        val vm = RecipeListViewModel(
            getRecipes = GetRecipesUseCase(repo),
            deleteRecipe = DeleteRecipeUseCase(repo),
            syncRecipes = SyncRecipesUseCase(repo),
            logout = LogoutUseCase(repo),
            getAuthState = GetAuthStateUseCase(repo),
            getEnrichments = GetEnrichmentsUseCase(FakeEnrichmentRepo()),
        )
        return vm to repo
    }

    @Test
    fun `sync success keeps isServerReachable true and clears error`() = runTest {
        val (vm, _) = buildViewModel(syncResult = Result.success(Unit))
        vm.sync()
        advanceUntilIdle()
        assertTrue(vm.state.value.isServerReachable)
        assertNull(vm.state.value.error)
    }

    @Test
    fun `sync network failure sets isServerReachable to false and shows error`() = runTest {
        val (vm, _) = buildViewModel(syncResult = Result.failure(Exception("Connection refused")))
        vm.sync()
        advanceUntilIdle()
        assertFalse(vm.state.value.isServerReachable)
        assertNotNull(vm.state.value.error)
    }

    @Test
    fun `sync SessionExpiredException triggers logout`() = runTest {
        val (vm, repo) = buildViewModel(syncResult = Result.failure(SessionExpiredException()))
        vm.sync()
        advanceUntilIdle()
        assertEquals(AuthState.LoggedOut, repo.authFlow.value)
    }

    @Test
    fun `sync SessionExpiredException does not set isServerReachable false`() = runTest {
        val (vm, _) = buildViewModel(syncResult = Result.failure(SessionExpiredException()))
        vm.sync()
        advanceUntilIdle()
        // Session expiry triggers logout, not the server-unreachable banner
        assertTrue(vm.state.value.isServerReachable)
    }

    @Test
    fun `isServerReachable recovers to true after a successful sync follows a failed one`() = runTest {
        val repo = FakeRecipeRepo(syncResult = Result.failure(Exception("down")))
        val vm = RecipeListViewModel(
            getRecipes = GetRecipesUseCase(repo),
            deleteRecipe = DeleteRecipeUseCase(repo),
            syncRecipes = SyncRecipesUseCase(repo),
            logout = LogoutUseCase(repo),
            getAuthState = GetAuthStateUseCase(repo),
            getEnrichments = GetEnrichmentsUseCase(FakeEnrichmentRepo()),
        )
        vm.sync()
        advanceUntilIdle()
        assertFalse(vm.state.value.isServerReachable)

        repo.syncResult = Result.success(Unit)
        vm.sync()
        advanceUntilIdle()
        assertTrue(vm.state.value.isServerReachable)
        assertNull(vm.state.value.error)
    }
}

private class FakeRecipeRepo(var syncResult: Result<Unit>) : RecipeRepository {
    val authFlow = MutableStateFlow<AuthState>(
        AuthState.LoggedIn(User(email = "test@test.com", serverUrl = "http://localhost", token = "tok"))
    )

    override fun getRecipes(order: RecipeOrder): Flow<List<Recipe>> = MutableStateFlow(emptyList())
    override suspend fun getRecipeById(id: String): Recipe? = null
    override suspend fun insertRecipe(recipe: Recipe) {}
    override suspend fun deleteRecipe(id: String) {}
    override suspend fun syncRecipes(): Result<Unit> = syncResult
    override suspend fun login(email: String, password: String, serverUrl: String) = Result.success(Unit)
    override suspend fun logout() { authFlow.value = AuthState.LoggedOut }
    override fun getAuthState(): Flow<AuthState> = authFlow
}

private class FakeEnrichmentRepo : EnrichmentRepository {
    override suspend fun getEnrichment(recipeId: String) = Result.success<RecipeEnrichment?>(null)
    override suspend fun getEnrichments() = Result.success(emptyList<RecipeEnrichment>())
}
