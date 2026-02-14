package com.rakuishi.rrr.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Insert
    suspend fun insert(activity: Activity): Long

    @Update
    suspend fun update(activity: Activity)

    @Query("SELECT * FROM activity ORDER BY created_at DESC")
    fun getAll(): Flow<List<Activity>>

    @Query("SELECT * FROM activity WHERE id = :id")
    suspend fun getById(id: Long): Activity?
}
