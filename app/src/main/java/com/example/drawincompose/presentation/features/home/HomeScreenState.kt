package com.example.drawincompose.presentation.features.home

import com.example.drawincompose.database.CanvasEntity

sealed interface HomeScreenState {
    data class Success(
        val canvases: List<CanvasState> = emptyList(),
        val isEditModeActive: Boolean = false,
        val selectedCanvases: Set<String> = setOf(),
    ) : HomeScreenState

    data object Loading : HomeScreenState

    data object Error : HomeScreenState
}

data class CanvasState(
    val id: String,
    val preview: ByteArray,
)

fun CanvasEntity.toState() = CanvasState(
    id = id,
    preview = preview,
)