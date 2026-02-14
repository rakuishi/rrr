package com.rakuishi.rrr.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PointDao {
    @Insert
    suspend fun insert(point: Point)

    @Query("SELECT * FROM point WHERE activity_id = :activityId ORDER BY timestamp ASC")
    suspend fun getByActivityId(activityId: Long): List<Point>
}
