package com.rakuishi.rrr.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity")
data class Activity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "total_time_ms") val totalTimeMs: Long = 0,
    @ColumnInfo(name = "total_distance_m") val totalDistanceM: Double = 0.0,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
)
