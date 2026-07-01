package com.ultraviolince.mykitchen.data.di

import com.ultraviolince.mykitchen.data.remote.EnrichmentApiClient
import com.ultraviolince.mykitchen.data.remote.RecipeApiClient
import com.ultraviolince.mykitchen.data.repository.EnrichmentRepositoryImpl
import com.ultraviolince.mykitchen.data.repository.RecipeRepositoryImpl
import com.ultraviolince.mykitchen.data.store.CredentialsStore
import com.ultraviolince.mykitchen.data.store.InMemoryCredentialsStore
import com.ultraviolince.mykitchen.domain.repository.EnrichmentRepository
import com.ultraviolince.mykitchen.domain.repository.RecipeRepository
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val dataModule = module {
    // HttpClient — platform default engine auto-selected via Ktor expect/actual
    // (CIO for JVM/Android/iOS, Js for WasmJs).
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            install(HttpTimeout) {
                // Default for normal calls; beautify/refine override this per-request
                // because local (CPU) LLM inference can take minutes.
                requestTimeoutMillis = 30_000
            }
        }
    }
    single { RecipeApiClient(get()) }
    single { EnrichmentApiClient(get()) }
    single<CredentialsStore> { InMemoryCredentialsStore() }
    single<RecipeRepository> { RecipeRepositoryImpl(get(), get(), get()) }
    single<EnrichmentRepository> { EnrichmentRepositoryImpl(get(), get()) }
}
