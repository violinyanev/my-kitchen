package com.ultraviolince.mykitchen.domain.di

import com.ultraviolince.mykitchen.domain.usecase.AddRecipeUseCase
import com.ultraviolince.mykitchen.domain.usecase.BeautifyRecipeUseCase
import com.ultraviolince.mykitchen.domain.usecase.DeleteEnrichmentUseCase
import com.ultraviolince.mykitchen.domain.usecase.DeleteRecipeUseCase
import com.ultraviolince.mykitchen.domain.usecase.GetAuthStateUseCase
import com.ultraviolince.mykitchen.domain.usecase.GetEnrichmentUseCase
import com.ultraviolince.mykitchen.domain.usecase.GetRecipeUseCase
import com.ultraviolince.mykitchen.domain.usecase.GetRecipesUseCase
import com.ultraviolince.mykitchen.domain.usecase.LoginUseCase
import com.ultraviolince.mykitchen.domain.usecase.LogoutUseCase
import com.ultraviolince.mykitchen.domain.usecase.RefineFeedbackUseCase
import com.ultraviolince.mykitchen.domain.usecase.SyncRecipesUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { GetRecipesUseCase(get()) }
    factory { GetRecipeUseCase(get()) }
    factory { AddRecipeUseCase(get()) }
    factory { DeleteRecipeUseCase(get()) }
    factory { SyncRecipesUseCase(get()) }
    factory { LoginUseCase(get()) }
    factory { LogoutUseCase(get()) }
    factory { GetAuthStateUseCase(get()) }
    factory { BeautifyRecipeUseCase(get()) }
    factory { GetEnrichmentUseCase(get()) }
    factory { RefineFeedbackUseCase(get()) }
    factory { DeleteEnrichmentUseCase(get()) }
}
