package com.ultraviolince.mykitchen

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.ultraviolince.mykitchen.ui.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "My Kitchen",
    ) {
        App()
    }
}
