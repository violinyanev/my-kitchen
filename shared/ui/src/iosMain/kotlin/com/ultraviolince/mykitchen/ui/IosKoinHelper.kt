package com.ultraviolince.mykitchen.ui

import com.ultraviolince.mykitchen.data.di.dataModule
import com.ultraviolince.mykitchen.data.di.platformDataModule
import com.ultraviolince.mykitchen.domain.di.domainModule
import com.ultraviolince.mykitchen.ui.di.uiModule
import org.koin.core.context.startKoin

/**
 * Called from the Swift entry point to bootstrap Koin DI.
 * Example Swift usage:
 *   IosKoinHelperKt.doInitKoin()
 */
fun initKoin() {
    startKoin {
        modules(platformDataModule, dataModule, domainModule, uiModule)
    }
}
