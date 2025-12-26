package com.example.spendsense

import android.app.Application
import com.example.spendsense.db.AppDatabase

class SpendSenseApplication : Application() {
    // The Application class ONLY provides the database instance.
    val database by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }
}