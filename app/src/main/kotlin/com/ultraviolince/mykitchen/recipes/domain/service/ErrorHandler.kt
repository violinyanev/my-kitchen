package com.ultraviolince.mykitchen.recipes.domain.service

interface ErrorHandler {
    fun handleError(error: Throwable): String
    fun isNetworkError(error: Throwable): Boolean
    fun isAuthError(error: Throwable): Boolean
}