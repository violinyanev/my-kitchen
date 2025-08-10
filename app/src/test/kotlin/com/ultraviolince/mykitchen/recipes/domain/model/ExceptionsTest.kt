package com.ultraviolince.mykitchen.recipes.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LoginExceptionTest {

    @Test
    fun `LoginException holds error message resource id`() {
        val errorMsgId = 12345
        val exception = LoginException(errorMsgId)

        assertThat(exception.errorMsg).isEqualTo(errorMsgId)
    }

    @Test
    fun `LoginException is an Exception`() {
        val exception = LoginException(123)

        assertThat(exception).isInstanceOf(Exception::class.java)
    }

    @Test
    fun `LoginException with zero resource id`() {
        val exception = LoginException(0)

        assertThat(exception.errorMsg).isEqualTo(0)
    }

    @Test
    fun `LoginException with negative resource id`() {
        val exception = LoginException(-1)

        assertThat(exception.errorMsg).isEqualTo(-1)
    }
}

class InvalidRecipeExceptionTest {

    @Test
    fun `InvalidRecipeException holds error string resource id`() {
        val errorStringId = 54321
        val exception = InvalidRecipeException(errorStringId)

        assertThat(exception.errorString).isEqualTo(errorStringId)
    }

    @Test
    fun `InvalidRecipeException is an Exception`() {
        val exception = InvalidRecipeException(123)

        assertThat(exception).isInstanceOf(Exception::class.java)
    }

    @Test
    fun `InvalidRecipeException with zero resource id`() {
        val exception = InvalidRecipeException(0)

        assertThat(exception.errorString).isEqualTo(0)
    }

    @Test
    fun `InvalidRecipeException with negative resource id`() {
        val exception = InvalidRecipeException(-1)

        assertThat(exception.errorString).isEqualTo(-1)
    }
}
