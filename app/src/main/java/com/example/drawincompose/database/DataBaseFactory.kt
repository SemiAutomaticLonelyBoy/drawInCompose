package com.example.drawincompose.database

import android.content.Context
import android.graphics.Color

import androidx.room.Room
import androidx.room.RoomDatabase

class DataBaseFactory(
    private val context: Context,
) {
    fun create(): RoomDatabase.Builder<CanvasDataBase> {
        val appContext = context.applicationContext
        val dbFile = appContext.getDatabasePath(CanvasDataBase.DB_NAME)

        return Room.databaseBuilder<CanvasDataBase>(
            context = context,
            name = dbFile.absolutePath,
        ).fallbackToDestructiveMigration(false)
    }
}