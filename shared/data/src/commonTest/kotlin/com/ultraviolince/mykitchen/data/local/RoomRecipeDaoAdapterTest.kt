package com.ultraviolince.mykitchen.data.local

import com.ultraviolince.mykitchen.domain.model.Recipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeRecipeRoomDao : RecipeRoomDao {
    private val state = MutableStateFlow<List<RecipeEntity>>(emptyList())

    override fun getRecipesByTitle(): Flow<List<RecipeEntity>> =
        state.map { it.filter { e -> !e.deleted }.sortedBy { e -> e.title } }

    override fun getRecipesByDate(): Flow<List<RecipeEntity>> =
        state.map { it.filter { e -> !e.deleted }.sortedByDescending { e -> e.timestamp } }

    override suspend fun getAllActive(): List<RecipeEntity> =
        state.value.filter { !it.deleted }

    override suspend fun getUnsyncedDeletedIds(): List<String> =
        state.value.filter { it.deleted && !it.synced }.map { it.id }

    override suspend fun getById(id: String): RecipeEntity? =
        state.value.find { it.id == id }

    override suspend fun insert(recipe: RecipeEntity) {
        val current = state.value.filter { it.id != recipe.id }.toMutableList()
        current.add(recipe)
        state.value = current
    }

    override suspend fun insertAll(recipes: List<RecipeEntity>) =
        recipes.forEach { insert(it) }

    override suspend fun softDelete(id: String) {
        state.value = state.value.map {
            if (it.id == id) it.copy(deleted = true, synced = false) else it
        }
    }

    override suspend fun markSynced(id: String) {
        state.value = state.value.map {
            if (it.id == id) it.copy(synced = true) else it
        }
    }

    override suspend fun clearSyncedDeleted() {
        state.value = state.value.filter { !(it.deleted && it.synced) }
    }
}

class RoomRecipeDaoAdapterTest {

    @Test
    fun `getRecipesByTitle returns active recipes sorted alphabetically`() = runTest {
        val fakeDao = FakeRecipeRoomDao()
        val adapter = RoomRecipeDaoAdapter(fakeDao)
        fakeDao.insert(RecipeEntity("1", "Zucchini", "z", 1000L, false, false))
        fakeDao.insert(RecipeEntity("2", "Apple", "a", 2000L, false, false))
        val result = adapter.getRecipesByTitle().first()
        assertEquals(2, result.size)
        assertEquals("Apple", result[0].title)
        assertEquals("Zucchini", result[1].title)
    }

    @Test
    fun `getRecipesByDate returns active recipes newest first`() = runTest {
        val fakeDao = FakeRecipeRoomDao()
        val adapter = RoomRecipeDaoAdapter(fakeDao)
        fakeDao.insert(RecipeEntity("1", "Old", "o", 1000L, false, false))
        fakeDao.insert(RecipeEntity("2", "New", "n", 2000L, false, false))
        val result = adapter.getRecipesByDate().first()
        assertEquals("New", result[0].title)
    }

    @Test
    fun `getAllActive excludes deleted recipes`() = runTest {
        val fakeDao = FakeRecipeRoomDao()
        val adapter = RoomRecipeDaoAdapter(fakeDao)
        fakeDao.insert(RecipeEntity("1", "Active", "a", 1000L, false, false))
        fakeDao.insert(RecipeEntity("2", "Deleted", "d", 2000L, false, true))
        val result = adapter.getAllActive()
        assertEquals(1, result.size)
        assertEquals("Active", result[0].title)
    }

    @Test
    fun `getUnsyncedDeletedIds returns ids of deleted-but-not-synced recipes`() = runTest {
        val fakeDao = FakeRecipeRoomDao()
        val adapter = RoomRecipeDaoAdapter(fakeDao)
        fakeDao.insert(RecipeEntity("id1", "ToSync", "t", 1000L, synced = false, deleted = true))
        val ids = adapter.getUnsyncedDeletedIds()
        assertEquals(listOf("id1"), ids)
    }

    @Test
    fun `getById returns recipe when it exists and null otherwise`() = runTest {
        val fakeDao = FakeRecipeRoomDao()
        val adapter = RoomRecipeDaoAdapter(fakeDao)
        fakeDao.insert(RecipeEntity("id1", "Pasta", "p", 1000L, false, false))
        assertNull(adapter.getById("missing"))
        assertEquals("Pasta", adapter.getById("id1")?.title)
    }

    @Test
    fun `insert adds recipe and insertAll adds multiple recipes`() = runTest {
        val fakeDao = FakeRecipeRoomDao()
        val adapter = RoomRecipeDaoAdapter(fakeDao)
        adapter.insert(Recipe.create("Pasta", "Cook"))
        adapter.insertAll(listOf(Recipe.create("Pizza", "Bake"), Recipe.create("Salad", "Mix")))
        assertEquals(3, adapter.getAllActive().size)
    }

    @Test
    fun `softDelete marks recipe as deleted so it no longer appears in active list`() = runTest {
        val fakeDao = FakeRecipeRoomDao()
        val adapter = RoomRecipeDaoAdapter(fakeDao)
        val recipe = Recipe.create("Pasta", "Cook")
        adapter.insert(recipe)
        adapter.softDelete(recipe.id)
        assertTrue(adapter.getAllActive().isEmpty())
    }

    @Test
    fun `markSynced and clearSyncedDeleted remove synced deleted recipes`() = runTest {
        val fakeDao = FakeRecipeRoomDao()
        val adapter = RoomRecipeDaoAdapter(fakeDao)
        val entity = RecipeEntity("id1", "Pasta", "p", 1000L, synced = false, deleted = true)
        fakeDao.insert(entity)
        adapter.markSynced("id1")
        adapter.clearSyncedDeleted()
        assertTrue(adapter.getAllActive().isEmpty())
        assertTrue(adapter.getUnsyncedDeletedIds().isEmpty())
    }
}
