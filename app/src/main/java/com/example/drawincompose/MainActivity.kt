package com.example.drawincompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.R
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.drawincompose.presentation.features.canvas.CanvasScreen
import com.example.drawincompose.presentation.features.home.HomeScreen
import com.example.drawincompose.ui.theme.DrawInComposeTheme
import kotlinx.serialization.Serializable

sealed interface Routes {
    @Serializable
    data object Home : Routes

    @Serializable
    data class Canvas(val id: String) : Routes
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DrawInComposeTheme(
                darkTheme = false,
            ) {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = Routes.Home,
                ) {
                    composable<Routes.Home> {
                        HomeScreen(
                            navController = navController,
                        )
                    }
                    composable<Routes.Canvas> { backStackEntry ->
                        val canvas = backStackEntry.toRoute<Routes.Canvas>()
                        CanvasScreen(
                            navController = navController,
                            canvasId = canvas.id,
                        )
                    }
                }
            }
        }
    }
}

