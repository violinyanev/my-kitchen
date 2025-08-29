package com.ultraviolince.mykitchen.recipes.presentation

import android.os.Bundle
import android.view.Choreographer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ultraviolince.mykitchen.recipes.presentation.navigation.EnhancedNavigationHost
import com.ultraviolince.mykitchen.recipes.presentation.navigation.NavigationRoutes
import com.ultraviolince.mykitchen.recipes.presentation.util.PerfTracer
import com.ultraviolince.mykitchen.ui.theme.MyApplicationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

@Composable
fun AppNavigationHost(modifier: Modifier = Modifier) {
    LaunchedEffect(Unit) {
        withContext(Dispatchers.Main) {
            Choreographer.getInstance().postFrameCallback {
                PerfTracer.endAsyncSection("AppStartup")
            }
        }
    }

    Surface(
        modifier = modifier.safeDrawingPadding().fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        EnhancedNavigationHost(
            startDestination = NavigationRoutes.RECIPES
        ) { navController, navigationActions, navigationObserver ->
            // The navigation graph is built inside EnhancedNavigationHost
            // Additional navigation logic can be added here if needed
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
