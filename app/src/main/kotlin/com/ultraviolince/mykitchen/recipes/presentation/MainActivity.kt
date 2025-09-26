package com.ultraviolince.mykitchen.recipes.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
// Import from shared module
import com.ultraviolince.mykitchen.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                AppNavigationHost()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppNavigationHostPreview() {
    MyApplicationTheme {
        AppNavigationHost()
    }
}
