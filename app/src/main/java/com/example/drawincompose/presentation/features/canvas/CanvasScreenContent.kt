package com.example.drawincompose.presentation.features.canvas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastRoundToInt
import com.example.drawincompose.presentation.features.home.HomeScreenModel
import com.example.drawincompose.utils.ShakeDetector
import kotlinx.coroutines.launch
import kotlin.math.abs

val allColors = listOf(
    Color.Black,
    Color.Red,
    Color.Blue,
    Color.Yellow,
    Color.Green,
    Color.Magenta,
    Color.Cyan,
    Color.White,
)

@Composable
fun CanvasScreenContent(
    paddingValues: PaddingValues,
    sendAction: (CanvasScreenModel.InputAction) -> Unit,
    state: CanvasScreenState,
) {

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DrawingScreen(
            paths = state.paths,
            currentPath = state.currentPath,
            sendAction = sendAction,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
        AnimatedVisibility(state.currentPath == null && state.isControlsVisible) {
            CanvasControls(
                selectedColor = state.selectedColor,
                colors = allColors,
                state = state,
                paddingValues = paddingValues,
                onThicknessChange = {
                    sendAction(CanvasScreenModel.InputAction.ChangePathThickness(it))
                },
                onSelectColor = {
                    sendAction(CanvasScreenModel.InputAction.OnSelectColor(it))
                },
                onClearCanvas = {
                    sendAction(CanvasScreenModel.InputAction.OnClearCanvasClick)
                },
            )
        }
    }
}

@Composable
fun DrawingScreen(
    paths: List<PathData>,
    currentPath: PathData?,
    sendAction: (CanvasScreenModel.InputAction) -> Unit,
    modifier: Modifier,
) {
    Canvas(
        modifier = modifier
            .clipToBounds()
            .background(Color.White)
            .pointerInput(true) {
                detectDragGestures(
                    onDragStart = {
                        sendAction(CanvasScreenModel.InputAction.OnNewPathStart)
                    },
                    onDrag = { change, _ ->
                        sendAction(CanvasScreenModel.InputAction.OnDraw(change.position))
                    },
                    onDragEnd = {
                        sendAction(CanvasScreenModel.InputAction.OnPathEnd)
                    },
                    onDragCancel = {
                        sendAction(CanvasScreenModel.InputAction.OnPathEnd)
                    }
                )
            }
    ) {
        paths.fastForEach { pathData ->
            drawPath(
                path = pathData.path,
                color = pathData.color,
                thickness = pathData.thickness * 5,
            )
        }
        currentPath?.let {
            drawPath(
                path = it.path,
                color = it.color,
                thickness = it.thickness * 5,
            )
        }
    }
}

private fun DrawScope.drawPath(
    path: List<Offset>,
    color: Color,
    thickness: Float,
) {
    val smoothedPath = Path().apply {
        if (path.isNotEmpty()) {
            moveTo(path.first().x, path.first().y)

            val smoothness = 5
            for (i in 1..path.lastIndex) {
                val from = path[i - 1]
                val to = path[i]
                val dx = abs(from.x - to.x)
                val dy = abs(from.y - to.y)
                if (dx >= smoothness || dy >= smoothness) {
                    quadraticTo(
                        x1 = (from.x + to.x) / 2f,
                        y1 = (from.y + to.y) / 2f,
                        x2 = to.x,
                        y2 = to.y
                    )
                }
            }
        }
    }
    drawPath(
        path = smoothedPath,
        color = color,
        style = Stroke(
            width = thickness,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        ),
    )
}

@Composable
fun CanvasControls(
    selectedColor: Color,
    colors: List<Color>,
    state: CanvasScreenState,
    onSelectColor: (Color) -> Unit,
    onClearCanvas: () -> Unit,
    onThicknessChange: (Float) -> Unit,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .background(color = Color(48, 48, 48, 38), shape = RoundedCornerShape(16.dp))
            .padding(bottom = paddingValues.calculateBottomPadding()),

        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                modifier = Modifier
                    .padding(end = 16.dp),
                text = state.thickness.fastRoundToInt().toString(),
            )
            Slider(
                value = state.thickness,
                onValueChange = {
                    onThicknessChange(it)
                },
                valueRange = 10f..50f,
                steps = 39,
                colors = SliderDefaults.colors(
                    thumbColor = Color(71, 103, 255, 181),
                ),
            )

        }
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        ) {
            colors.fastForEach { color ->
                val isSelected = color == selectedColor
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            val scale = if (isSelected) 1.2f else 1f
                            scaleX = scale
                            scaleY = scale
                        }
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color = color)
                        .border(
                            width = 2.dp,
                            color = if (isSelected) Color.Black else Color.Transparent,
                            shape = CircleShape,
                        )
                        .clickable {
                            onSelectColor(color)
                        },
                )
            }
        }
        Button(
            onClick = onClearCanvas,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(71, 103, 255, 181),
            ),
        ) {
            Text("Очистить холст")
        }
    }
}