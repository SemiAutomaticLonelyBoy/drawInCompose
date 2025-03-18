package com.example.drawincompose.presentation.features.canvas

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drawincompose.database.CanvasEntity
import com.example.drawincompose.database.DefaultCanvasRepository
import com.example.drawincompose.database.PathDataEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import kotlin.math.abs

class CanvasScreenModel(
    private val repository: DefaultCanvasRepository,
) : ViewModel() {

    private val mutableState: MutableStateFlow<CanvasScreenState> =
        MutableStateFlow(CanvasScreenState())
    val state: StateFlow<CanvasScreenState> = mutableState.asStateFlow()

    private val mutableActions: Channel<OutputAction> = Channel()
    val actions: Flow<OutputAction> = mutableActions.receiveAsFlow()
    private var _canvasId: String = ""

    fun input(action: InputAction) {
        when (action) {
            InputAction.OnClearCanvasClick -> clearCanvas()
            is InputAction.OnDraw -> onDraw(action.offset)
            InputAction.OnNewPathStart -> onNewPathStart()
            InputAction.OnPathEnd -> onPathEnd()
            is InputAction.OnSelectColor -> onSelectColor(action.color)
            InputAction.OnBackClick -> navigateToHomeScreen()
            is InputAction.OnSaveClick -> saveCanvas(action.height, action.width)
            is InputAction.Start -> getCanvas(action.canvasId)
            is InputAction.SetControlsVisible -> closeControls(action.isVisible)
            is InputAction.ChangePathThickness -> changePathThickness(action.thickness)
        }
    }

    private fun changePathThickness(thickness: Float) {
        mutableState.update { it.copy(
            thickness = thickness,
        ) }
    }

    private fun closeControls(isVisible: Boolean) {
        mutableState.update { it.copy(
            isControlsVisible = isVisible
        ) }
    }

    private fun getCanvas(canvasId: String) {
        _canvasId = canvasId
        if (canvasId.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            val canvas: List<PathDataEntity> = repository.getPathsForCanvas(canvasId)
            mutableState.update {
                it.copy(
                    paths = canvas.map { path -> path.toState() }
                )
            }
        }
    }

    private fun pathsToByteArray(paths: List<PathData>, height: Int, width: Int): ByteArray  {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).asImageBitmap()

        val canvas = Canvas(bitmap)

        paths.forEach { pathData: PathData ->

            val paint = Paint().apply {
                color = pathData.color
                strokeWidth = pathData.thickness * 5
                style = PaintingStyle.Stroke
            }

            val smoothedPath = Path().apply {
                if (pathData.path.isNotEmpty()) {
                    moveTo(pathData.path.first().x, pathData.path.first().y)

                    val smoothness = 5
                    for (i in 1..pathData.path.lastIndex) {
                        val from = pathData.path[i - 1]
                        val to = pathData.path[i]
                        val dx = abs(from.x - to.x)
                        val dy = abs(from.y - to.y)
                        if (dx >= smoothness || dy >= smoothness) {
                            quadraticTo(
                                x1 = (from.x + to.x) / 2f,
                                y1 = (from.y + to.y) / 2f,
                                x2 = to.x,
                                y2 = to.y,
                            )
                        }
                    }
                }
            }

            canvas.drawPath(
                path = smoothedPath,
                paint = paint,
            )
        }

        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    private fun saveCanvas(height: Int, width: Int) {
        val canvasId = _canvasId.ifBlank { System.currentTimeMillis().toString() }

        viewModelScope.launch(Dispatchers.IO) {
            repository.saveCanvas(
                CanvasEntity(
                    id = canvasId,
                    preview = pathsToByteArray(
                        paths = state.value.paths,
                        height = height,
                        width = width,
                    ),
                )
            ).also {
                state.value.paths.forEach { path ->
                    repository.insertPathData(
                        PathDataEntity(
                            id = path.id,
                            color = path.color,
                            path = path.path,
                            canvasId = canvasId,
                            thickness = path.thickness,
                        )
                    )
                }
            }
        }
    }

    private fun navigateToHomeScreen() {
        viewModelScope.launch {
            mutableActions.send(OutputAction.GoBack)
        }
    }

    private fun onSelectColor(color: Color) {
        mutableState.update {
            it.copy(
                selectedColor = color,
            )
        }
    }

    private fun onPathEnd() {
        val currentPathData = state.value.currentPath ?: return
        mutableState.update {
            it.copy(
                currentPath = null,
                paths = it.paths + currentPathData
            )
        }
    }

    private fun onDraw(offset: Offset) {
        val currentPathData = state.value.currentPath ?: return
        mutableState.update {
            it.copy(
                currentPath = currentPathData.copy(
                    path = currentPathData.path + offset,
                ),
            )
        }
    }

    private fun clearCanvas() {
        mutableState.update {
            it.copy(
                currentPath = null,
                paths = emptyList(),
            )
        }
    }

    private fun onNewPathStart() {
        mutableState.update {
            it.copy(
                currentPath = PathData(
                    id = System.currentTimeMillis().toString(),
                    color = it.selectedColor,
                    path = emptyList(),
                    thickness = it.thickness,
                )
            )
        }
    }

    sealed interface OutputAction {
        data object GoBack : OutputAction
    }

    sealed interface InputAction {
        data class Start(val canvasId: String) : InputAction
        data object OnNewPathStart : InputAction
        data class OnDraw(val offset: Offset) : InputAction
        data object OnPathEnd : InputAction
        data class OnSelectColor(val color: Color) : InputAction
        data object OnClearCanvasClick : InputAction
        data object OnBackClick : InputAction
        data class OnSaveClick(val height: Int, val width: Int) : InputAction
        data class SetControlsVisible(val isVisible: Boolean) : InputAction
        data class ChangePathThickness(val thickness: Float) : InputAction
    }

}