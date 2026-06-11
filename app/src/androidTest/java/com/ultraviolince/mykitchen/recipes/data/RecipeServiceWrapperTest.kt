package com.ultraviolince.mykitchen.recipes.data

import androidx.test.platform.app.InstrumentationRegistry
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.RecipeServiceWrapper
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.util.NetworkError
import com.ultraviolince.mykitchen.recipes.data.datasource.datastore.SafeDataStore
import com.ultraviolince.mykitchen.recipes.data.datasource.localdb.RecipeDao
import com.ultraviolince.mykitchen.recipes.data.datasource.localdb.entity.Recipe as LocalRecipe
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.recipes.domain.repository.LoginState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class FakeRecipeDao : RecipeDao {
    override fun getRecipes(): Flow<List<LocalRecipe>> = flowOf(emptyList())
    override suspend fun getRecipeById(id: Long): LocalRecipe? = null
    override suspend fun insertRecipe(recipe: LocalRecipe): Long = recipe.id ?: 0L
    override suspend fun insertRecipes(recipes: List<LocalRecipe>) = Unit
    override suspend fun deleteRecipe(recipe: LocalRecipe) = Unit
}

class RecipeServiceWrapperTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val dataStore = SafeDataStore(context)
    private val dao = FakeRecipeDao()
    private lateinit var service: RecipeServiceWrapper

    @Before
    fun setup() = runBlocking {
        dataStore.write("", "")
        service = RecipeServiceWrapper(dataStore, dao)
    }

    @Test
    fun logsInToServerSuccessfully() = runTest {
        service.login(FakeBackend.server, email = FakeBackend.testUser, FakeBackend.testPassword)
        assertEquals(LoginState.LoginSuccess, service.loginState.value)
    }

    @Test
    fun failsToLoginToServerWhenServerUriIsMalformed() = runTest {
        service.login("not valid address", email = FakeBackend.testUser, FakeBackend.testPassword)
        assertEquals(LoginState.LoginFailure(NetworkError.SERVER_ERROR), service.loginState.value)
    }

    @Test
    fun failsToLoginToServerWhenPasswordIsWrong() = runTest {
        service.login(FakeBackend.server, email = FakeBackend.testUser, "bad password")
        assertEquals(LoginState.LoginFailure(NetworkError.UNAUTHORIZED), service.loginState.value)
    }

    @Test
    fun testRecipesCreateDelete() = runTest {
        val fakeId = 5L

        service.login(FakeBackend.server, email = FakeBackend.testUser, FakeBackend.testPassword)
        assertEquals(LoginState.LoginSuccess, service.loginState.value)

        val created = service.insertRecipe(
            recipeId = fakeId,
            recipe = Recipe(
                id = fakeId,
                title = "title",
                content = "body",
                timestamp = 5L
            )
        )
        assertTrue(created)

        val deleted = service.deleteRecipe(recipeId = fakeId)
        assertTrue(deleted)
    }
}
