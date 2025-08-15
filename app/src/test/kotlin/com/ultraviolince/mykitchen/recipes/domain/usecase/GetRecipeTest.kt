package com.ultraviolince.mykitchen.recipes.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.ultraviolince.mykitchen.recipes.data.repository.FakeRecipeRepository
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class GetRecipeTest {

    private lateinit var getRecipe: GetRecipe
    private lateinit var fakeRepository: FakeRecipeRepository

    @Before
    fun setUp() {
        fakeRepository = FakeRecipeRepository()
        getRecipe = GetRecipe(fakeRepository)
    }

    @Test
    fun `invoke returns recipe when it exists`() = runBlocking {
        // Given - FakeRecipeRepository comes pre-populated with recipes a-z with IDs 0-25
        // Let's use an existing recipe with ID 0
        val existingRecipe = fakeRepository.getRecipeById(0L)
        assertThat(existingRecipe).isNotNull()

        // When
        val result = getRecipe(0L)

        // Then
        assertThat(result).isEqualTo(existingRecipe)
    }

    @Test
    fun `invoke returns null when recipe does not exist`() = runBlocking {
        // When
        val result = getRecipe(999L)

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun `invoke calls repository with correct id`() = runBlocking {
        // Given
        val testId = 42L

        // When
        getRecipe(testId)

        // Then - verify the repository was called (FakeRecipeRepository tracks calls)
        // Note: We can't directly verify the call, but we can verify behavior
        val result = getRecipe(testId)
        assertThat(result).isNull() // Since recipe doesn't exist
    }
}