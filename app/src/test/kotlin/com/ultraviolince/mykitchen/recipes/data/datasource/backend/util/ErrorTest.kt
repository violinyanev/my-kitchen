package com.ultraviolince.mykitchen.recipes.data.datasource.backend.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ErrorTest {

    // Test class implementing Error interface for testing
    private class TestError : Error

    @Test
    fun `Error interface can be implemented`() {
        val error: Error = TestError()

        assertThat(error).isInstanceOf(Error::class.java)
    }

    @Test
    fun `NetworkError implements Error interface`() {
        val networkError: Error = NetworkError.UNKNOWN

        assertThat(networkError).isInstanceOf(Error::class.java)
    }

    @Test
    fun `multiple Error implementations are different types`() {
        val networkError: Error = NetworkError.UNKNOWN
        val testError: Error = TestError()

        assertThat(networkError::class).isNotEqualTo(testError::class)
    }
}
