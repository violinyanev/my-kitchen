package com.ultraviolince.mykitchen

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.ultraviolince.mykitchen.data.di.dataModule
import com.ultraviolince.mykitchen.data.di.platformDataModule
import com.ultraviolince.mykitchen.domain.di.domainModule
import com.ultraviolince.mykitchen.ui.App
import com.ultraviolince.mykitchen.ui.di.uiModule
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    startKoin {
        modules(platformDataModule, dataModule, domainModule, uiModule)
    }
    CanvasBasedWindow(title = "My Kitchen", canvasElementId = "ComposeTarget") {
        App()
    }
}

