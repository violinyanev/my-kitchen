package com.ultraviolince.mykitchen

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.ultraviolince.mykitchen.ui.App

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow(title = "My Kitchen", canvasElementId = "ComposeTarget") {
        App()
    }
}
