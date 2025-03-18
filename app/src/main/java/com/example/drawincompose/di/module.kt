package com.example.drawincompose.di

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.example.drawincompose.database.CanvasDataBase
import com.example.drawincompose.database.DataBaseFactory
import org.koin.core.module.dsl.viewModelOf
import com.example.drawincompose.presentation.features.home.HomeScreenModel
import com.example.drawincompose.presentation.features.canvas.CanvasScreenModel
import com.example.drawincompose.database.DefaultCanvasRepository
import com.example.drawincompose.repository.CanvasRepository
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val modules = module {
    // viewModels
    viewModelOf(::HomeScreenModel)
    viewModelOf(::CanvasScreenModel)

    // useCase

    // repository
    singleOf(::DefaultCanvasRepository).bind<CanvasRepository>()

    single { DataBaseFactory(androidApplication()) }
    single { get<CanvasDataBase>().canvasDao}

    single {
        get<DataBaseFactory>().create()
            .setDriver(BundledSQLiteDriver())
            .build()
    }

}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE path_data ALTER COLUMN color TEXT")
    }
}