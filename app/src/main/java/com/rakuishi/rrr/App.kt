package com.rakuishi.rrr

import android.app.Application
import com.rakuishi.rrr.data.db.AppDatabase
import com.rakuishi.rrr.data.repository.ActivityRepository

class App : Application() {

    lateinit var repository: ActivityRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getInstance(this)
        repository = ActivityRepository(db.activityDao(), db.pointDao())
    }
}
