package com.ultraviolince.mykitchen.domain.usecase

import com.ultraviolince.mykitchen.domain.fake.FakeRecipeRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class SyncRecipesUseCaseTest {

    @Test
    fun `returns success when repo sync succeeds`() = runTest {
        val repo = FakeRecipeRepository()
        repo.syncResult = Result.success(Unit)
        val result = SyncRecipesUseCase(repo)()
        assertTrue(result.isSuccess)
    }

    @Test
    fun `returns failure when repo sync fails`() = runTest {
        val repo = FakeRecipeRepository()
        repo.syncResult = Result.failure(Exception("network error"))
        val result = SyncRecipesUseCase(repo)()
        assertTrue(result.isFailure)
    }
}
