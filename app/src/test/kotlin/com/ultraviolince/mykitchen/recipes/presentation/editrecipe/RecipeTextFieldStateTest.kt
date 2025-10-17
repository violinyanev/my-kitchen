package com.ultraviolince.mykitchen.recipes.presentation.editrecipe

import androidx.core.content.res.ResourcesCompat.ID_NULL
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RecipeTextFieldStateTest {

    @Test
    fun should_createDefaultState_when_noParametersProvided() {
        // When
        val state = RecipeTextFieldState()

        // Then
        assertThat(state.text).isEqualTo("")
        assertThat(state.hintStringId).isEqualTo(ID_NULL)
        assertThat(state.isHintVisible).isFalse()
    }

    @Test
    fun should_createStateWithText_when_textProvided() {
        // Given
        val text = "Recipe title text"

        // When
        val state = RecipeTextFieldState(text = text)

        // Then
        assertThat(state.text).isEqualTo(text)
        assertThat(state.hintStringId).isEqualTo(ID_NULL)
        assertThat(state.isHintVisible).isFalse()
    }

    @Test
    fun should_createStateWithHint_when_hintIdProvided() {
        // Given
        val hintId = android.R.string.ok

        // When
        val state = RecipeTextFieldState(hintStringId = hintId)

        // Then
        assertThat(state.text).isEqualTo("")
        assertThat(state.hintStringId).isEqualTo(hintId)
        assertThat(state.isHintVisible).isFalse()
    }

    @Test
    fun should_createStateWithHintVisible_when_hintVisibilitySet() {
        // Given
        val isHintVisible = true

        // When
        val state = RecipeTextFieldState(isHintVisible = isHintVisible)

        // Then
        assertThat(state.text).isEqualTo("")
        assertThat(state.hintStringId).isEqualTo(ID_NULL)
        assertThat(state.isHintVisible).isTrue()
    }

    @Test
    fun should_createStateWithAllParameters_when_allProvided() {
        // Given
        val text = "Complete recipe text"
        val hintId = android.R.string.copy
        val isHintVisible = true

        // When
        val state = RecipeTextFieldState(
            text = text,
            hintStringId = hintId,
            isHintVisible = isHintVisible
        )

        // Then
        assertThat(state.text).isEqualTo(text)
        assertThat(state.hintStringId).isEqualTo(hintId)
        assertThat(state.isHintVisible).isTrue()
    }

    @Test
    fun should_supportCopy_when_copyingWithChanges() {
        // Given
        val original = RecipeTextFieldState("original", android.R.string.ok, false)

        // When
        val copied = original.copy(text = "updated")

        // Then
        assertThat(copied.text).isEqualTo("updated")
        assertThat(copied.hintStringId).isEqualTo(original.hintStringId)
        assertThat(copied.isHintVisible).isEqualTo(original.isHintVisible)
        assertThat(copied).isNotEqualTo(original)
    }

    @Test
    fun should_supportEquality_when_comparingStates() {
        // Given
        val state1 = RecipeTextFieldState("text", android.R.string.ok, true)
        val state2 = RecipeTextFieldState("text", android.R.string.ok, true)
        val state3 = RecipeTextFieldState("different", android.R.string.ok, true)

        // Then
        assertThat(state1).isEqualTo(state2)
        assertThat(state1).isNotEqualTo(state3)
    }

    @Test
    fun should_supportHashCode_when_hashCodeCalled() {
        // Given
        val state1 = RecipeTextFieldState("same", android.R.string.ok, true)
        val state2 = RecipeTextFieldState("same", android.R.string.ok, true)

        // Then
        assertThat(state1.hashCode()).isEqualTo(state2.hashCode())
    }

    @Test
    fun should_supportToString_when_toStringCalled() {
        // Given
        val state = RecipeTextFieldState("test", android.R.string.ok, false)

        // When
        val stringRepresentation = state.toString()

        // Then
        assertThat(stringRepresentation).contains("RecipeTextFieldState")
        assertThat(stringRepresentation).contains("test")
    }
}