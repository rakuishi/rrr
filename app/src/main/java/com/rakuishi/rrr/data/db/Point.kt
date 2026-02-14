package com.rakuishi.rrr.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "point",
    foreignKeys = [
        ForeignKey(
            entity = Activity::class,
            parentColumns = ["id"],
            childColumns = ["activity_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("activity_id")],
)
data class Point(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "activity_id") val activityId: Long,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis(),
)
