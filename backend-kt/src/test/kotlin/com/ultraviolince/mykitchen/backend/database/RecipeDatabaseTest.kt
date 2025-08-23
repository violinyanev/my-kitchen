package com.ultraviolince.mykitchen.backend.database

import com.ultraviolince.mykitchen.backend.model.Recipe
import com.ultraviolince.mykitchen.backend.model.RecipeRequest
import com.ultraviolince.mykitchen.backend.model.User
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteIfExists

class RecipeDatabaseTest {
    private lateinit var testFile: Path
    private val testUser = User("Joe", "joe@example.com", "password123")

    @Before
    fun setUp() {
        testFile = Files.createTempFile("test_database", ".yaml")
    }

    @After
    fun tearDown() {
        testFile.deleteIfExists()
    }

    @Test
    fun testInit() {
        val db = RecipeDatabase(testFile)
        assertTrue(Files.exists(testFile))
        assertEquals(emptyList<Recipe>(), db.get(testUser, all = false))
    }

    @Test
    fun testPutValidRecipe() {
        val db = RecipeDatabase(testFile)
        val recipeRequest = RecipeRequest(
            id = 1,
            title = "Test Recipe",
            body = "Recipe body",
            timestamp = 12L
        )
        
        val (newRecipe, error) = db.put(testUser, recipeRequest)
        assertNull(error)
        assertNotNull(newRecipe)
        assertEquals(1, newRecipe?.id)
        assertEquals("Test Recipe", newRecipe?.title)
        assertEquals("Recipe body", newRecipe?.body)
        assertEquals(12L, newRecipe?.timestamp)
        assertEquals(testUser.name, newRecipe?.user)
    }

    @Test
    fun testPutRecipeWithoutId() {
        val db = RecipeDatabase(testFile)
        val recipeRequest = RecipeRequest(
            title = "Test Recipe",
            body = "Recipe body"
        )
        
        val (newRecipe, error) = db.put(testUser, recipeRequest)
        assertNull(error)
        assertNotNull(newRecipe)
        assertEquals(1, newRecipe?.id) // Should auto-assign ID 1
        assertEquals("Test Recipe", newRecipe?.title)
        assertEquals(testUser.name, newRecipe?.user)
    }

    @Test
    fun testPutRecipeEmptyTitle() {
        val db = RecipeDatabase(testFile)
        val recipeRequest = RecipeRequest(
            title = "",
            body = "Recipe body"
        )
        
        val (newRecipe, error) = db.put(testUser, recipeRequest)
        assertNull(newRecipe)
        assertEquals("Recipe title can't be empty", error)
    }

    @Test
    fun testPutRecipeConflictingId() {
        val db = RecipeDatabase(testFile)
        val recipeRequest1 = RecipeRequest(id = 1, title = "First Recipe")
        val recipeRequest2 = RecipeRequest(id = 1, title = "Second Recipe")
        
        db.put(testUser, recipeRequest1)
        val (newRecipe, error) = db.put(testUser, recipeRequest2)
        
        assertNull(newRecipe)
        assertEquals("Recipe with id 1 exists!", error)
    }

    @Test
    fun testGetUserRecipes() {
        val db = RecipeDatabase(testFile)
        val user1 = User("User1", "user1@example.com", "pass")
        val user2 = User("User2", "user2@example.com", "pass")
        
        db.put(user1, RecipeRequest(title = "User1 Recipe"))
        db.put(user2, RecipeRequest(title = "User2 Recipe"))
        
        val user1Recipes = db.get(user1, all = false)
        assertEquals(1, user1Recipes.size)
        assertEquals("User1 Recipe", user1Recipes[0].title)
        assertEquals("User1", user1Recipes[0].user)
        
        val allRecipes = db.get(user1, all = true)
        assertEquals(2, allRecipes.size)
    }

    @Test
    fun testDeleteRecipe() {
        val db = RecipeDatabase(testFile)
        val recipeRequest = RecipeRequest(id = 1, title = "Test Recipe")
        
        val (recipe, _) = db.put(testUser, recipeRequest)
        assertNotNull(recipe)
        
        val (success, result) = db.delete(testUser, 1)
        assertTrue(success)
        assertEquals(recipe, result)
        
        val recipes = db.get(testUser, all = false)
        assertEquals(0, recipes.size)
    }

    @Test
    fun testDeleteNonExistentRecipe() {
        val db = RecipeDatabase(testFile)
        
        val (success, result) = db.delete(testUser, 999)
        assertFalse(success)
        assertEquals("There is no recipe with id 999", result)
    }

    @Test
    fun testDeleteRecipeNotOwned() {
        val db = RecipeDatabase(testFile)
        val user1 = User("User1", "user1@example.com", "pass")
        val user2 = User("User2", "user2@example.com", "pass")
        
        db.put(user1, RecipeRequest(id = 1, title = "User1 Recipe"))
        
        val (success, result) = db.delete(user2, 1)
        assertFalse(success)
        assertEquals("Recipe 1 does not belong to you, you can't delete it!", result)
    }
}