package com.ultraviolince.mykitchen.data.di

import com.ultraviolince.mykitchen.data.remote.RecipeApiClient
import com.ultraviolince.mykitchen.data.repository.RecipeRepositoryImpl
import com.ultraviolince.mykitchen.domain.repository.RecipeRepository
import io.ktor.client.HttpClient
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
        }
    }
    single { RecipeApiClient(get()) }
    single<RecipeRepository> { RecipeRepositoryImpl(get(), get(), get()) }
}
