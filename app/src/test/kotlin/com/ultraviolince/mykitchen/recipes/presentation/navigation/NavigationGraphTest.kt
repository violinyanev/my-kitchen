package com.ultraviolince.mykitchen.recipes.presentation.navigation

import androidx.navigation.NavType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class NavigationGraphTest {
    
    @Test
    fun `NavigationRoutes should have correct route constants`() {
        assertEquals("login", NavigationRoutes.LOGIN)
        assertEquals("recipes", NavigationRoutes.RECIPES)
        assertEquals("add_edit_recipe", NavigationRoutes.ADD_EDIT_RECIPE)
        assertEquals("recipeId", NavigationRoutes.RECIPE_ID_ARG)
    }
    
    @Test
    fun `NavigationArgs should have correct argument definition`() {
        val recipeIdArg = NavigationArgs.recipeId
        
        assertNotNull(recipeIdArg)
        assertEquals(NavigationRoutes.RECIPE_ID_ARG, recipeIdArg.name)
        assertEquals(NavType.IntType, recipeIdArg.type)
        assertEquals(-1, recipeIdArg.defaultValue)
    }
    
    @Test
    fun `add edit recipe route should be correctly formatted`() {
        val expectedRoute = "${NavigationRoutes.ADD_EDIT_RECIPE}/{${NavigationRoutes.RECIPE_ID_ARG}}"
        val actualRoute = "${NavigationRoutes.ADD_EDIT_RECIPE}/{${NavigationRoutes.RECIPE_ID_ARG}}"
        
        assertEquals(expectedRoute, actualRoute)
        assertEquals("add_edit_recipe/{recipeId}", actualRoute)
    }
    
    @Test
    fun `navigation routes should be unique`() {
        val routes = listOf(
            NavigationRoutes.LOGIN,
            NavigationRoutes.RECIPES,
            NavigationRoutes.ADD_EDIT_RECIPE
        )
        
        val uniqueRoutes = routes.toSet()
        assertEquals(routes.size, uniqueRoutes.size)
    }
    
    @Test
    fun `navigation argument names should be unique`() {
        val argNames = listOf(
            NavigationRoutes.RECIPE_ID_ARG
        )
        
        val uniqueArgNames = argNames.toSet()
        assertEquals(argNames.size, uniqueArgNames.size)
    }
    
    @Test
    fun `recipe id argument should have correct default value`() {
        val recipeIdArg = NavigationArgs.recipeId
        
        assertEquals(-1, recipeIdArg.defaultValue)
    }
    
    @Test
    fun `recipe id argument should be of integer type`() {
        val recipeIdArg = NavigationArgs.recipeId
        
        assertEquals(NavType.IntType, recipeIdArg.type)
    }
    
    @Test
    fun `navigation routes should not contain special characters`() {
        val routes = listOf(
            NavigationRoutes.LOGIN,
            NavigationRoutes.RECIPES,
            NavigationRoutes.ADD_EDIT_RECIPE
        )
        
        routes.forEach { route ->
            assertTrue("Route '$route' should not contain spaces", !route.contains(" "))
            assertTrue("Route '$route' should not contain special characters", 
                route.matches(Regex("^[a-zA-Z0-9_]+$")))
        }
    }
    
    @Test
    fun `navigation argument names should not contain special characters`() {
        val argNames = listOf(
            NavigationRoutes.RECIPE_ID_ARG
        )
        
        argNames.forEach { argName ->
            assertTrue("Argument name '$argName' should not contain spaces", !argName.contains(" "))
            assertTrue("Argument name '$argName' should not contain special characters", 
                argName.matches(Regex("^[a-zA-Z0-9_]+$")))
        }
    }
    
    @Test
    fun `navigation routes should be lowercase`() {
        val routes = listOf(
            NavigationRoutes.LOGIN,
            NavigationRoutes.RECIPES,
            NavigationRoutes.ADD_EDIT_RECIPE
        )
        
        routes.forEach { route ->
            assertEquals(route, route.lowercase())
        }
    }
    
    @Test
    fun `navigation argument names should be camelCase`() {
        val argNames = listOf(
            NavigationRoutes.RECIPE_ID_ARG
        )
        
        argNames.forEach { argName ->
            assertTrue("Argument name '$argName' should be in camelCase", 
                argName.matches(Regex("^[a-z][a-zA-Z0-9]*$")))
        }
    }
    
    private fun assertTrue(message: String, condition: Boolean) {
        if (!condition) {
            throw AssertionError(message)
        }
    }
}