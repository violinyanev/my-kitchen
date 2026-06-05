package com.ultraviolince.mykitchen

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.ultraviolince.mykitchen.data.di.dataModule
import com.ultraviolince.mykitchen.data.di.platformDataModule
import com.ultraviolince.mykitchen.domain.di.domainModule
import com.ultraviolince.mykitchen.ui.App
import com.ultraviolince.mykitchen.ui.di.uiModule
import org.koin.core.context.startKoin

fun main() {
    startKoin {
        modules(platformDataModule, dataModule, domainModule, uiModule)
    }
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "My Kitchen",
        ) {
            App()
        }
    }
}
