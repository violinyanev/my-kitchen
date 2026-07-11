package com.ultraviolince.mykitchen

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.ultraviolince.mykitchen.data.di.dataModule
import com.ultraviolince.mykitchen.data.di.platformDataModule
import com.ultraviolince.mykitchen.domain.di.domainModule
import com.ultraviolince.mykitchen.ui.App
import com.ultraviolince.mykitchen.ui.DefaultCredentials
import com.ultraviolince.mykitchen.ui.di.uiModule
import kotlinx.browser.document
import kotlinx.browser.window
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val queryParams = parseQueryParams()
    val defaultCredentials = queryParams.takeIf { it.isNotEmpty() }?.let {
        DefaultCredentials(
            serverUrl = it["url"],
            email = it["user"],
            password = it["pass"],
        )
    }

    startKoin {
        modules(platformDataModule, dataModule, domainModule, uiModule)
    }
    ComposeViewport(document.body!!) {
        App(defaultCredentials = defaultCredentials)
    }
}

private fun parseQueryParams(): Map<String, String> {
    val search = window.location.search.removePrefix("?")
    if (search.isBlank()) return emptyMap()
    return search.split("&").mapNotNull { param ->
        val eq = param.indexOf('=')
        if (eq < 0) null else param.substring(0, eq) to param.substring(eq + 1)
    }.toMap()
}

