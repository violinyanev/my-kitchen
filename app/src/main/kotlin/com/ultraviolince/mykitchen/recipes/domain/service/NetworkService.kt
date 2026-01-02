package com.ultraviolince.mykitchen.recipes.domain.service

import io.ktor.client.HttpClient

interface NetworkService {
    fun createHttpClient(server: String, token: String?): HttpClient
}
