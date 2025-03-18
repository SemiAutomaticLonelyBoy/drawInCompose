package com.example.drawincompose.database

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

@Database(
    entities = [CanvasEntity::class, PathDataEntity::class],
    version = 4,
)
@TypeConverters(
    Converters::class
)
abstract class CanvasDataBase : RoomDatabase() {
    abstract val canvasDao: CanvasDao

    companion object {
        const val DB_NAME = "canvas.db"
    }
}

@Entity(tableName = "canvas")
data class CanvasEntity(
    @PrimaryKey(autoGenerate = false) val id: String,
    val preview: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CanvasEntity

        if (id != other.id) return false
        if (!preview.contentEquals(other.preview)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + preview.contentHashCode()
        return result
    }
}

@Entity(
    tableName = "path_data",
    foreignKeys = [
        ForeignKey(
            entity = CanvasEntity::class,
            parentColumns = ["id"],
            childColumns = ["canvasId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@TypeConverters(Converters::class)
data class PathDataEntity(
    @PrimaryKey(autoGenerate = false) val id: String,
    val canvasId: String,
    val thickness: Float,
    val color: Color,
    val path: List<Offset>,
)

class Converters {
    @TypeConverter
    fun fromColor(color: Color): String {
        return color.value.toString()
    }

    @TypeConverter
    fun toColor(value: String): Color {
        return Color(value.toULong())
    }

    @TypeConverter
    fun fromOffsetList(offsets: List<Offset>): String {
        return offsets.joinToString(";") { "${it.x},${it.y}" }
    }

    @TypeConverter
    fun toOffsetList(value: String): List<Offset> {
        return value.split(";").map {
            val (x, y) = it.split(",")
            Offset(x.toFloat(), y.toFloat())
        }
    }
}