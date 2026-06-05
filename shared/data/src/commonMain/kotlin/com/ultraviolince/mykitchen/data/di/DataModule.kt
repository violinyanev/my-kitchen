package com.ultraviolince.mykitchen.data.di

import com.ultraviolince.mykitchen.data.remote.RecipeApiClient
import com.ultraviolince.mykitchen.data.repository.RecipeRepositoryImpl
import com.ultraviolince.mykitchen.data.store.CredentialsStore
import com.ultraviolince.mykitchen.data.store.InMemoryCredentialsStore
import com.ultraviolince.mykitchen.domain.repository.RecipeRepository
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val dataModule = module {
    // HttpClient — engine is provided by a platform-specific Koin module (wired in Phase 6)
    single {
        HttpClient(get()) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }
    single { RecipeApiClient(get()) }
    single<CredentialsStore> { InMemoryCredentialsStore() }
    single<RecipeRepository> { RecipeRepositoryImpl(get(), get(), get()) }
}
