package com.example.drawincompose.presentation.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.drawincompose.R
import com.example.drawincompose.Routes
import com.example.drawincompose.presentation.features.canvas.CanvasScreenState
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
) {

    val viewModel: HomeScreenModel = koinViewModel()
    val state: HomeScreenState = viewModel.state.collectAsState().value

    LaunchedEffect(Unit) {
        viewModel.actions.collect { action ->
            when (action) {
                is HomeScreenModel.OutputAction.GoToCanvasScreen -> {
                    navController.navigate(Routes.Canvas(action.id))
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(top = 30.dp),
                title = {
                    when (state) {
                        is HomeScreenState.Success -> {
                            if (state.isEditModeActive) {
                                Text("Редактирование")
                            } else {
                                Text("Холсты")
                            }
                        }

                        else -> Text("Холсты")
                    }

                },
                navigationIcon = {
                    when (state) {
                        is HomeScreenState.Success -> {
                            if (state.isEditModeActive) {
                                IconButton(
                                    onClick = {
                                        viewModel.input(HomeScreenModel.InputAction.DisableEditMode)
                                    },
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.baseline_close_24),
                                        tint = Color(141, 141, 141),
                                        contentDescription = null,
                                    )
                                }
                            } else {
                                Unit
                            }
                        }

                        else -> Unit
                    }

                },
                actions = {
                    when (state) {
                        is HomeScreenState.Success -> {
                            if (state.isEditModeActive) {
                                IconButton(
                                    onClick = {
                                        viewModel.input(HomeScreenModel.InputAction.OnDeleteIconClick)
                                    },
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.baseline_delete_24),
                                        tint = Color(141, 141, 141),
                                        contentDescription = null,
                                    )
                                }
                            } else {
                                Unit
                            }
                        }

                        else -> Unit
                    }

                },
                colors = TopAppBarDefaults.topAppBarColors(
                    titleContentColor = Color(45, 48, 56),
                ),
            )
        }
    ) { paddingValues ->

        HomeScreenContent(
            paddingValues = paddingValues,
            sendAction = viewModel::input,
            state = state,
        )
    }
}
