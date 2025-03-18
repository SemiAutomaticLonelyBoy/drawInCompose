package com.example.drawincompose.presentation.features.canvas

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.window.layout.WindowMetricsCalculator
import com.example.drawincompose.R
import com.example.drawincompose.utils.ShakeDetector
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanvasScreen(
    navController: NavController,
    canvasId: String,
) {

    val viewModel: CanvasScreenModel = koinViewModel()
    val state: CanvasScreenState = viewModel.state.collectAsState().value

    LaunchedEffect(Unit) {
        viewModel.input(CanvasScreenModel.InputAction.Start(canvasId))
        viewModel.actions.collect { action ->
            when(action) {
                CanvasScreenModel.OutputAction.GoBack -> {
                    navController.popBackStack()
                }
            }
        }
    }

    val context = LocalContext.current
    val activity = context as Activity
    val windowMetrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(activity)

    val width = windowMetrics.bounds.width()
    val height = windowMetrics.bounds.height()

    val coroutineScope = rememberCoroutineScope()

    val shakeDetector = remember {
        ShakeDetector(context) {
            coroutineScope.launch {
                viewModel.input(CanvasScreenModel.InputAction.SetControlsVisible(true))
            }
        }
    }

    LaunchedEffect(Unit) {
        shakeDetector.start()
    }

    DisposableEffect(Unit) {
        onDispose {
            shakeDetector.stop()
        }
    }

    Scaffold(
        topBar = {
            AnimatedVisibility(state.currentPath == null  && state.isControlsVisible) {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        FloatIcon {
                            IconButton(
                                onClick = {
                                    viewModel.input(CanvasScreenModel.InputAction.OnBackClick)
                                }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_arrow_back_24),
                                    tint = Color(141,141,141),
                                    contentDescription = null,
                                )
                            }
                        }
                    },
                    actions = {
                        FloatIcon {
                            IconButton(
                                onClick = {
                                    viewModel.input(CanvasScreenModel.InputAction.SetControlsVisible(false))
                                }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_fullscreen_24),
                                    tint = Color(141,141,141),
                                    contentDescription = null,
                                )
                            }
                        }
                        FloatIcon {
                            IconButton(
                                onClick = {
                                    viewModel.input(CanvasScreenModel.InputAction.OnSaveClick(
                                        height = height,
                                        width = width,
                                    ))
                                }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_save_24),
                                    tint = Color(141,141,141),
                                    contentDescription = null,
                                )
                            }
                        }

                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                    ),
                )
            }

        },

    ) { paddingValues ->
        CanvasScreenContent(
            paddingValues = paddingValues,
            sendAction = viewModel::input,
            state = state,
        )
    }
}

@Composable
fun FloatIcon(
    icon: @Composable () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .graphicsLayer(
                shadowElevation = with(LocalDensity.current) { 5.dp.toPx() },
                shape = RoundedCornerShape(16.dp),
                clip = false,
            )
            .background(color = Color.White, shape = RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center,
    ) {
        icon()
    }
}