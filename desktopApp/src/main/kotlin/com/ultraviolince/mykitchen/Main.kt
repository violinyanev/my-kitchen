package com.ultraviolince.mykitchen

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.ultraviolince.mykitchen.data.di.dataModule
import com.ultraviolince.mykitchen.data.di.platformDataModule
import com.ultraviolince.mykitchen.domain.di.domainModule
import com.ultraviolince.mykitchen.ui.App
import com.ultraviolince.mykitchen.ui.DefaultCredentials
import com.ultraviolince.mykitchen.ui.di.uiModule
import org.koin.core.context.startKoin

fun main(args: Array<String>) {
    val argMap = args
        .filter { it.startsWith("--") && it.contains("=") }
        .associate { arg ->
            val eq = arg.indexOf('=')
            arg.substring(2, eq) to arg.substring(eq + 1)
        }
    val defaultCredentials = argMap.takeIf { it.isNotEmpty() }?.let {
        DefaultCredentials(
            serverUrl = it["url"],
            email = it["user"],
            password = it["pass"],
        )
    }

    startKoin {
        modules(platformDataModule, dataModule, domainModule, uiModule)
    }
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "My Kitchen",
        ) {
            App(defaultCredentials = defaultCredentials)
        }
    }
}
