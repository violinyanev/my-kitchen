package com.ultraviolince.mykitchen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.ultraviolince.mykitchen.ui.App
import com.ultraviolince.mykitchen.ui.DefaultCredentials

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val defaultCredentials = BuildConfig.PREVIEW_SERVER_URL
            .takeIf { it.isNotEmpty() }
            ?.let {
                DefaultCredentials(
                    serverUrl = BuildConfig.PREVIEW_SERVER_URL,
                    email = BuildConfig.PREVIEW_EMAIL,
                    password = BuildConfig.PREVIEW_PASSWORD,
                )
            }
        setContent {
            App(defaultCredentials = defaultCredentials)
        }
    }
}
