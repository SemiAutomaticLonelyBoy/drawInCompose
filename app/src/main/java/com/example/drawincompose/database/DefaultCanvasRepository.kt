package com.example.drawincompose.database

import com.example.drawincompose.repository.CanvasRepository
import kotlinx.coroutines.flow.Flow

class DefaultCanvasRepository(
    private val canvasDao: CanvasDao,
) : CanvasRepository {
    override suspend fun getCanvases(): Flow<List<CanvasEntity>> {
        return canvasDao.getCanvases()
    }

    override suspend fun saveCanvas(canvas: CanvasEntity) {
        canvasDao.insertCanvas(canvas)
    }

    override suspend fun insertPathData(path: PathDataEntity) {
        canvasDao.insertPathData(path)
    }

    override fun getPathsForCanvas(canvasId: String): List<PathDataEntity> {
        return canvasDao.getPathsForCanvas(canvasId)
    }

    override fun deleteCanvases(canvases: List<String>) {
        canvasDao.deleteCanvases(canvases)
    }
}