package com.ultraviolince.mykitchen.recipes.data.datasource.backend.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class NetworkErrorTest {

    @Test
    fun `NetworkError implements Error interface`() {
        val error: Error = NetworkError.UNKNOWN
        assertThat(error).isInstanceOf(Error::class.java)
    }

    @Test
    fun `all NetworkError values are accessible`() {
        val allErrors = listOf(
            NetworkError.REQUEST_TIMEOUT,
            NetworkError.UNAUTHORIZED,
            NetworkError.CONFLICT,
            NetworkError.TOO_MANY_REQUESTS,
            NetworkError.NO_INTERNET,
            NetworkError.PAYLOAD_TOO_LARGE,
            NetworkError.SERVER_ERROR,
            NetworkError.SERIALIZATION,
            NetworkError.UNKNOWN
        )

        assertThat(allErrors).hasSize(9)
        assertThat(NetworkError.values().toList()).containsExactlyElementsIn(allErrors)
    }

    @Test
    fun `NetworkError enum values are unique`() {
        val values = NetworkError.values()
        val uniqueValues = values.toSet()

        assertThat(uniqueValues).hasSize(values.size)
    }

    @Test
    fun `specific NetworkError values have correct names`() {
        assertThat(NetworkError.REQUEST_TIMEOUT.name).isEqualTo("REQUEST_TIMEOUT")
        assertThat(NetworkError.UNAUTHORIZED.name).isEqualTo("UNAUTHORIZED")
        assertThat(NetworkError.NO_INTERNET.name).isEqualTo("NO_INTERNET")
        assertThat(NetworkError.UNKNOWN.name).isEqualTo("UNKNOWN")
    }
}
