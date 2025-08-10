package com.ultraviolince.mykitchen.recipes.data.datasource.backend.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ResultTest {

    @Test
    fun `Success result holds data correctly`() {
        val data = "test data"
        val result = Result.Success(data)

        assertThat(result.data).isEqualTo(data)
    }

    @Test
    fun `Error result holds error correctly`() {
        val error = NetworkError.UNAUTHORIZED
        val result = Result.Error(error)

        assertThat(result.error).isEqualTo(error)
    }

    @Test
    fun `map transforms success data`() {
        val result = Result.Success(5)
        val mapped = result.map { it * 2 }

        assertThat(mapped).isInstanceOf(Result.Success::class.java)
        assertThat((mapped as Result.Success).data).isEqualTo(10)
    }

    @Test
    fun `map preserves error`() {
        val error = NetworkError.SERVER_ERROR
        val result: Result<Int, NetworkError> = Result.Error(error)
        val mapped = result.map { it * 2 }

        assertThat(mapped).isInstanceOf(Result.Error::class.java)
        assertThat((mapped as Result.Error).error).isEqualTo(error)
    }

    @Test
    fun `asEmptyDataResult converts success to unit`() {
        val result = Result.Success("data")
        val empty = result.asEmptyDataResult()

        assertThat(empty).isInstanceOf(Result.Success::class.java)
        assertThat((empty as Result.Success).data).isEqualTo(Unit)
    }

    @Test
    fun `asEmptyDataResult preserves error`() {
        val error = NetworkError.NO_INTERNET
        val result: Result<String, NetworkError> = Result.Error(error)
        val empty = result.asEmptyDataResult()

        assertThat(empty).isInstanceOf(Result.Error::class.java)
        assertThat((empty as Result.Error).error).isEqualTo(error)
    }

    @Test
    fun `onSuccess executes action for success`() {
        var actionExecuted = false
        var capturedData: String? = null

        val result = Result.Success("test")
        val returned = result.onSuccess {
            actionExecuted = true
            capturedData = it
        }

        assertThat(actionExecuted).isTrue()
        assertThat(capturedData).isEqualTo("test")
        assertThat(returned).isSameInstanceAs(result)
    }

    @Test
    fun `onSuccess does not execute action for error`() {
        var actionExecuted = false

        val result: Result<String, NetworkError> = Result.Error(NetworkError.UNKNOWN)
        val returned = result.onSuccess { actionExecuted = true }

        assertThat(actionExecuted).isFalse()
        assertThat(returned).isSameInstanceAs(result)
    }

    @Test
    fun `onError executes action for error`() {
        var actionExecuted = false
        var capturedError: NetworkError? = null

        val result: Result<String, NetworkError> = Result.Error(NetworkError.REQUEST_TIMEOUT)
        val returned = result.onError {
            actionExecuted = true
            capturedError = it
        }

        assertThat(actionExecuted).isTrue()
        assertThat(capturedError).isEqualTo(NetworkError.REQUEST_TIMEOUT)
        assertThat(returned).isSameInstanceAs(result)
    }

    @Test
    fun `onError does not execute action for success`() {
        var actionExecuted = false

        val result = Result.Success("data")
        val returned = result.onError { actionExecuted = true }

        assertThat(actionExecuted).isFalse()
        assertThat(returned).isSameInstanceAs(result)
    }

    @Test
    fun `chaining map and onSuccess works correctly`() {
        var capturedValue: Int? = null

        val result = Result.Success(10)
            .map { it * 2 }
            .onSuccess { capturedValue = it }

        assertThat(result).isInstanceOf(Result.Success::class.java)
        assertThat((result as Result.Success).data).isEqualTo(20)
        assertThat(capturedValue).isEqualTo(20)
    }

    @Test
    fun `chaining map and onError with error preserves error`() {
        var errorCaptured = false

        val errorResult: Result<Int, NetworkError> = Result.Error(NetworkError.CONFLICT)
        val result = errorResult
            .map { value: Int -> value * 2 }
            .onError { errorCaptured = true }

        assertThat(result).isInstanceOf(Result.Error::class.java)
        assertThat((result as Result.Error).error).isEqualTo(NetworkError.CONFLICT)
        assertThat(errorCaptured).isTrue()
    }

    @Test
    fun `multiple map transformations work correctly`() {
        val result = Result.Success(5)
            .map { it * 2 } // 10
            .map { it + 3 } // 13
            .map { it.toString() } // "13"

        assertThat(result).isInstanceOf(Result.Success::class.java)
        assertThat((result as Result.Success).data).isEqualTo("13")
    }

    @Test
    fun `EmptyResult type alias works correctly`() {
        val emptySuccess: EmptyResult<NetworkError> = Result.Success(Unit)
        val emptyError: EmptyResult<NetworkError> = Result.Error(NetworkError.SERIALIZATION)

        assertThat(emptySuccess).isInstanceOf(Result.Success::class.java)
        assertThat(emptyError).isInstanceOf(Result.Error::class.java)
        assertThat((emptySuccess as Result.Success).data).isEqualTo(Unit)
        assertThat((emptyError as Result.Error).error).isEqualTo(NetworkError.SERIALIZATION)
    }
}
