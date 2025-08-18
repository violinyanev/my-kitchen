package com.ultraviolince.mykitchen.recipes.data.service

import android.util.Log
import com.ultraviolince.mykitchen.recipes.data.datasource.backend.createHttpClient
import com.ultraviolince.mykitchen.recipes.domain.service.NetworkService
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.logging.Logger
import org.koin.core.annotation.Single

@Single
class NetworkServiceImpl : NetworkService {

    private val logger = object : Logger {
        override fun log(message: String) {
            Log.d("#network #ktor #data", message)
        }
    }

    override fun createHttpClient(server: String, token: String?): HttpClient {
        return createHttpClient(CIO.create(), server, token, logger)
    }
}
