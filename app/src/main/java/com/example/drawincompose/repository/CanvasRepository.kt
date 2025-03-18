package com.example.drawincompose.repository

import com.example.drawincompose.database.CanvasEntity
import com.example.drawincompose.database.PathDataEntity
import kotlinx.coroutines.flow.Flow

interface CanvasRepository {
    suspend fun getCanvases(): Flow<List<CanvasEntity>>
    suspend fun saveCanvas(canvas: CanvasEntity)
    suspend fun insertPathData(path: PathDataEntity)
    fun getPathsForCanvas(canvasId: String): List<PathDataEntity>
    fun deleteCanvases(canvases: List<String>)
}

