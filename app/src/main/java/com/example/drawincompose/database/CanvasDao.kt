package com.example.drawincompose.database

import androidx.compose.ui.graphics.Canvas
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CanvasDao {

    @Query("SELECT * FROM canvas")
    fun getCanvases(): Flow<List<CanvasEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCanvas(canvas: CanvasEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPathData(path: PathDataEntity)

    @Query("SELECT * FROM path_data WHERE canvasId = :canvasId")
    fun getPathsForCanvas(canvasId: String): List<PathDataEntity>

    @Query("DELETE FROM canvas WHERE id IN (:canvases)")
    fun deleteCanvases(canvases: List<String>)

}