package com.ultraviolince.mykitchen.recipes.data.analytics

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AnalyticsConfigTest {

    @Test
    fun `default configuration values are set`() {
        assertThat(AnalyticsConfig.DEFAULT_SERVER_URL).isNotEmpty()
        assertThat(AnalyticsConfig.DEFAULT_APP_KEY).isNotEmpty()
        assertThat(AnalyticsConfig.isEnabled).isTrue()
    }

    @Test
    fun `event names are defined`() {
        assertThat(AnalyticsConfig.Events.APP_STARTED).isEqualTo("app_started")
        assertThat(AnalyticsConfig.Events.RECIPE_OPERATION).isEqualTo("recipe_operation")
        assertThat(AnalyticsConfig.Events.AUTH_OPERATION).isEqualTo("auth_operation")
        assertThat(AnalyticsConfig.Events.NAVIGATION).isEqualTo("navigation")
    }

    @Test
    fun `segmentation keys are defined`() {
        assertThat(AnalyticsConfig.Segmentation.ACTION).isEqualTo("action")
        assertThat(AnalyticsConfig.Segmentation.SCREEN).isEqualTo("screen")
        assertThat(AnalyticsConfig.Segmentation.SUCCESS).isEqualTo("success")
        assertThat(AnalyticsConfig.Segmentation.RECIPE_ID).isEqualTo("recipe_id")
    }

    @Test
    fun `recipe actions are defined`() {
        assertThat(AnalyticsConfig.RecipeActions.CREATE).isEqualTo("create")
        assertThat(AnalyticsConfig.RecipeActions.READ).isEqualTo("read")
        assertThat(AnalyticsConfig.RecipeActions.UPDATE).isEqualTo("update")
        assertThat(AnalyticsConfig.RecipeActions.DELETE).isEqualTo("delete")
        assertThat(AnalyticsConfig.RecipeActions.LIST).isEqualTo("list")
    }

    @Test
    fun `auth actions are defined`() {
        assertThat(AnalyticsConfig.AuthActions.LOGIN).isEqualTo("login")
        assertThat(AnalyticsConfig.AuthActions.LOGOUT).isEqualTo("logout")
        assertThat(AnalyticsConfig.AuthActions.LOGIN_ATTEMPT).isEqualTo("login_attempt")
    }

    @Test
    fun `screen names are defined`() {
        assertThat(AnalyticsConfig.Screens.RECIPE_LIST).isEqualTo("recipe_list")
        assertThat(AnalyticsConfig.Screens.RECIPE_DETAIL).isEqualTo("recipe_detail")
        assertThat(AnalyticsConfig.Screens.RECIPE_EDIT).isEqualTo("recipe_edit")
        assertThat(AnalyticsConfig.Screens.LOGIN).isEqualTo("login")
    }

    @Test
    fun `isEnabled can be modified`() {
        val originalValue = AnalyticsConfig.isEnabled
        
        AnalyticsConfig.isEnabled = false
        assertThat(AnalyticsConfig.isEnabled).isFalse()
        
        AnalyticsConfig.isEnabled = true
        assertThat(AnalyticsConfig.isEnabled).isTrue()
        
        // Restore original value
        AnalyticsConfig.isEnabled = originalValue
    }
}