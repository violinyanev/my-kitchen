package com.ultraviolince.mykitchen.recipes.presentation.editrecipe

import androidx.compose.ui.focus.FocusState
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import org.junit.Test

class AddEditRecipeEventTest {

    @Test
    fun should_createEnteredTitleEvent_when_valueProvided() {
        // Given
        val title = "Test Recipe Title"

        // When
        val event = AddEditRecipeEvent.EnteredTitle(title)

        // Then
        assertThat(event).isInstanceOf(AddEditRecipeEvent.EnteredTitle::class.java)
        assertThat(event.value).isEqualTo(title)
    }

    @Test
    fun should_createChangeTitleFocusEvent_when_focusStateProvided() {
        // Given
        val focusState = mockk<FocusState>()

        // When
        val event = AddEditRecipeEvent.ChangeTitleFocus(focusState)

        // Then
        assertThat(event).isInstanceOf(AddEditRecipeEvent.ChangeTitleFocus::class.java)
        assertThat(event.focusState).isEqualTo(focusState)
    }

    @Test
    fun should_createEnteredContentEvent_when_valueProvided() {
        // Given
        val content = "Recipe instructions and content"

        // When
        val event = AddEditRecipeEvent.EnteredContent(content)

        // Then
        assertThat(event).isInstanceOf(AddEditRecipeEvent.EnteredContent::class.java)
        assertThat(event.value).isEqualTo(content)
    }

    @Test
    fun should_createChangeContentFocusEvent_when_focusStateProvided() {
        // Given
        val focusState = mockk<FocusState>()

        // When
        val event = AddEditRecipeEvent.ChangeContentFocus(focusState)

        // Then
        assertThat(event).isInstanceOf(AddEditRecipeEvent.ChangeContentFocus::class.java)
        assertThat(event.focusState).isEqualTo(focusState)
    }

    @Test
    fun should_createSaveRecipeEvent_when_accessed() {
        // When
        val event = AddEditRecipeEvent.SaveRecipe

        // Then
        assertThat(event).isInstanceOf(AddEditRecipeEvent.SaveRecipe::class.java)
        assertThat(event).isEqualTo(AddEditRecipeEvent.SaveRecipe)
    }

    @Test
    fun should_createDeleteRecipeEvent_when_accessed() {
        // When
        val event = AddEditRecipeEvent.DeleteRecipe

        // Then
        assertThat(event).isInstanceOf(AddEditRecipeEvent.DeleteRecipe::class.java)
        assertThat(event).isEqualTo(AddEditRecipeEvent.DeleteRecipe)
    }

    @Test
    fun should_haveDifferentEventTypes_when_comparing() {
        // Given
        val titleEvent = AddEditRecipeEvent.EnteredTitle("title")
        val contentEvent = AddEditRecipeEvent.EnteredContent("content")
        val saveEvent = AddEditRecipeEvent.SaveRecipe
        val deleteEvent = AddEditRecipeEvent.DeleteRecipe

        // Then
        assertThat(titleEvent).isNotEqualTo(contentEvent)
        assertThat(titleEvent).isNotEqualTo(saveEvent)
        assertThat(titleEvent).isNotEqualTo(deleteEvent)
        assertThat(saveEvent).isNotEqualTo(deleteEvent)
    }

    @Test
    fun should_equalSameEvents_when_sameDataProvided() {
        // Given
        val title1 = AddEditRecipeEvent.EnteredTitle("same title")
        val title2 = AddEditRecipeEvent.EnteredTitle("same title")
        val title3 = AddEditRecipeEvent.EnteredTitle("different title")

        // Then
        assertThat(title1).isEqualTo(title2)
        assertThat(title1).isNotEqualTo(title3)
    }
}