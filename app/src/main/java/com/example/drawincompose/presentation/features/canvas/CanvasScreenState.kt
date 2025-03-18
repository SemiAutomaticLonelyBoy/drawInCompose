package com.example.drawincompose.presentation.features.canvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.drawincompose.database.PathDataEntity

data class CanvasScreenState(
    val selectedColor: Color = Color.Black,
    val currentPath: PathData? = null,
    val thickness: Float = 2f,
    val paths: List<PathData> = emptyList(),
    val isControlsVisible: Boolean = true,
)

data class PathData(
    val id: String,
    val color: Color,
    val path: List<Offset>,
    val thickness: Float,
)

fun PathDataEntity.toState() = PathData(
    id = id,
    color = color,
    path = path,
    thickness = thickness,
)