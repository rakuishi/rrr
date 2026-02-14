package com.rakuishi.rrr.data.repository

import com.rakuishi.rrr.data.db.Activity
import com.rakuishi.rrr.data.db.ActivityDao
import com.rakuishi.rrr.data.db.Point
import com.rakuishi.rrr.data.db.PointDao
import kotlinx.coroutines.flow.Flow

class ActivityRepository(
    private val activityDao: ActivityDao,
    private val pointDao: PointDao,
) {
    suspend fun createActivity(): Long {
        return activityDao.insert(Activity())
    }

    suspend fun updateActivity(activity: Activity) {
        activityDao.update(activity)
    }

    suspend fun getActivity(id: Long): Activity? {
        return activityDao.getById(id)
    }

    fun getAllActivities(): Flow<List<Activity>> {
        return activityDao.getAll()
    }

    suspend fun insertPoint(point: Point) {
        pointDao.insert(point)
    }

    suspend fun getPoints(activityId: Long): List<Point> {
        return pointDao.getByActivityId(activityId)
    }

    suspend fun deleteActivity(id: Long) {
        activityDao.deleteById(id)
    }
}
