package com.ultraviolince.mykitchen.recipes.presentation.navigation

import androidx.navigation.NavController
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class NavigationActionsTest {
    
    @MockK
    private lateinit var mockNavController: NavController
    
    private lateinit var navigationActions: NavigationActions
    
    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        navigationActions = NavigationActions(mockNavController)
    }
    
    @Test
    fun `navigateToLogin should navigate to login route and clear back stack`() {
        val popUpToSlot = slot<Int>()
        val inclusiveSlot = slot<Boolean>()
        
        every {
            mockNavController.navigate(
                NavigationRoutes.LOGIN,
                any(),
                any(),
                capture(popUpToSlot),
                capture(inclusiveSlot)
            )
        } returns Unit
        
        navigationActions.navigateToLogin()
        
        verify {
            mockNavController.navigate(
                NavigationRoutes.LOGIN,
                any(),
                any(),
                any(),
                any()
            )
        }
        
        assertEquals(0, popUpToSlot.captured)
        assertEquals(true, inclusiveSlot.captured)
    }
    
    @Test
    fun `navigateToRecipes should navigate to recipes route and pop up to login`() {
        val popUpToSlot = slot<String>()
        val inclusiveSlot = slot<Boolean>()
        
        every {
            mockNavController.navigate(
                NavigationRoutes.RECIPES,
                any(),
                any(),
                capture(popUpToSlot),
                capture(inclusiveSlot)
            )
        } returns Unit
        
        navigationActions.navigateToRecipes()
        
        verify {
            mockNavController.navigate(
                NavigationRoutes.RECIPES,
                any(),
                any(),
                any(),
                any()
            )
        }
        
        assertEquals(NavigationRoutes.LOGIN, popUpToSlot.captured)
        assertEquals(true, inclusiveSlot.captured)
    }
    
    @Test
    fun `navigateToAddRecipe should navigate to add edit recipe with default recipe id`() {
        val expectedRoute = "${NavigationRoutes.ADD_EDIT_RECIPE}/-1"
        
        every {
            mockNavController.navigate(expectedRoute)
        } returns Unit
        
        navigationActions.navigateToAddRecipe()
        
        verify {
            mockNavController.navigate(expectedRoute)
        }
    }
    
    @Test
    fun `navigateToEditRecipe should navigate to add edit recipe with specific recipe id`() {
        val recipeId = 123
        val expectedRoute = "${NavigationRoutes.ADD_EDIT_RECIPE}/$recipeId"
        
        every {
            mockNavController.navigate(expectedRoute)
        } returns Unit
        
        navigationActions.navigateToEditRecipe(recipeId)
        
        verify {
            mockNavController.navigate(expectedRoute)
        }
    }
    
    @Test
    fun `navigateBack should call popBackStack on nav controller`() {
        every {
            mockNavController.popBackStack()
        } returns true
        
        navigationActions.navigateBack()
        
        verify {
            mockNavController.popBackStack()
        }
    }
    
    @Test
    fun `navigateToRecipesAndClearStack should navigate to recipes and clear entire back stack`() {
        val popUpToSlot = slot<Int>()
        val inclusiveSlot = slot<Boolean>()
        
        every {
            mockNavController.navigate(
                NavigationRoutes.RECIPES,
                any(),
                any(),
                capture(popUpToSlot),
                capture(inclusiveSlot)
            )
        } returns Unit
        
        navigationActions.navigateToRecipesAndClearStack()
        
        verify {
            mockNavController.navigate(
                NavigationRoutes.RECIPES,
                any(),
                any(),
                any(),
                any()
            )
        }
        
        assertEquals(0, popUpToSlot.captured)
        assertEquals(true, inclusiveSlot.captured)
    }
    
    @Test
    fun `navigation routes should have correct values`() {
        assertEquals("login", NavigationRoutes.LOGIN)
        assertEquals("recipes", NavigationRoutes.RECIPES)
        assertEquals("add_edit_recipe", NavigationRoutes.ADD_EDIT_RECIPE)
        assertEquals("recipeId", NavigationRoutes.RECIPE_ID_ARG)
    }
    
    @Test
    fun `navigation actions should handle multiple consecutive calls`() {
        every {
            mockNavController.navigate(any<String>())
        } returns Unit
        
        every {
            mockNavController.navigate(any<String>(), any(), any(), any(), any())
        } returns Unit
        
        every {
            mockNavController.popBackStack()
        } returns true
        
        // Test multiple navigation calls
        navigationActions.navigateToAddRecipe()
        navigationActions.navigateToEditRecipe(456)
        navigationActions.navigateBack()
        navigationActions.navigateToRecipes()
        
        verify(exactly = 1) {
            mockNavController.navigate("${NavigationRoutes.ADD_EDIT_RECIPE}/-1")
        }
        
        verify(exactly = 1) {
            mockNavController.navigate("${NavigationRoutes.ADD_EDIT_RECIPE}/456")
        }
        
        verify(exactly = 1) {
            mockNavController.popBackStack()
        }
        
        verify(exactly = 1) {
            mockNavController.navigate(NavigationRoutes.RECIPES, any(), any(), any(), any())
        }
    }
    
    @Test
    fun `navigation actions should handle different recipe ids`() {
        every {
            mockNavController.navigate(any<String>())
        } returns Unit
        
        val recipeIds = listOf(0, 1, 100, -1, 999)
        
        recipeIds.forEach { recipeId ->
            navigationActions.navigateToEditRecipe(recipeId)
        }
        
        recipeIds.forEach { recipeId ->
            verify(exactly = 1) {
                mockNavController.navigate("${NavigationRoutes.ADD_EDIT_RECIPE}/$recipeId")
            }
        }
    }
}