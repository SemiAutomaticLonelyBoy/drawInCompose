package com.example.drawincompose.presentation.features.home

import android.graphics.BitmapFactory
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.drawincompose.R
import okhttp3.internal.Util

@Composable
fun HomeScreenContent(
    paddingValues: PaddingValues,
    sendAction: (HomeScreenModel.InputAction) -> Unit,
    state: HomeScreenState,
) {

    when(state) {
        HomeScreenState.Error -> Unit
        HomeScreenState.Loading -> Loading()
        is HomeScreenState.Success -> Success(
            paddingValues = paddingValues,
            sendAction = sendAction,
            state = state,
        )
    }
}

@Composable
fun Success(
    paddingValues: PaddingValues,
    sendAction: (HomeScreenModel.InputAction) -> Unit,
    state: HomeScreenState.Success,
) {

    LazyVerticalGrid(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding(),
            ),
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(state.canvases) { index, canvas ->
            val isContains = state.selectedCanvases.contains(canvas.id)
            val scale: Float by animateFloatAsState(
                targetValue = if (isContains) 0.93f else 1f,
                label = "FloatAnimation",
            )
            Box(
                modifier = Modifier
                    .height(200.dp)
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        shadowElevation = with(LocalDensity.current) { 5.dp.toPx() },
                        shape = RoundedCornerShape(16.dp),
                        clip = false,
                    )
                    .background(color = Color.White, shape = RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .pointerInput(state.selectedCanvases) {
                        detectTapGestures(
                            onLongPress = {
                                sendAction(HomeScreenModel.InputAction.SetEditMode(true))
                                sendAction(HomeScreenModel.InputAction.SetCanvasSelected(canvas.id))
                            },
                            onTap = {
                                if (state.isEditModeActive) {
                                    sendAction(HomeScreenModel.InputAction.SetCanvasSelected(canvas.id))
                                } else {
                                    sendAction(HomeScreenModel.InputAction.OnCanvasClick(canvas.id))
                                }
                            },
                        )
                    },
            ) {

                val bitmap = BitmapFactory.decodeByteArray(canvas.preview, 0, canvas.preview.size)
                Image(
                    modifier = Modifier.fillMaxSize(),
                    bitmap = bitmap.asImageBitmap(),
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                )

                if (state.isEditModeActive) {
                    Icon(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        painter = if (isContains) {
                            painterResource(R.drawable.baseline_check_circle_24)
                        } else {
                            painterResource(R.drawable.baseline_radio_button_unchecked_24)
                        },
                        tint = Color(71, 103, 255, 181),
                        contentDescription = null,
                    )
                }

            }
        }
        if (state.isEditModeActive) return@LazyVerticalGrid
        item {
            Card(
                modifier = Modifier
                    .height(200.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(241, 241, 241),
                    disabledContainerColor = Color.White,
                ),
                onClick = {
                    sendAction(HomeScreenModel.InputAction.OnCanvasClick(""))
                },
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        modifier = Modifier
                            .size(140.dp),
                        tint = Color(200, 200, 200),
                        painter = painterResource(R.drawable.baseline_add_24),
                        contentDescription = null,
                    )
                }
            }
        }
    }
}

@Composable
fun Loading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = Color(71, 103, 255, 181)
        )
    }
}

@Composable
fun Error() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = Color(71, 103, 255, 181)
        )
    }
}
