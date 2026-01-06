package com.example.spendsense

import android.app.Application

class SpendSenseApplication : Application() {

    // WE REMOVED THE DATABASE AND REPOSITORY PROPERTIES FROM HERE.
    // They are now created on-demand in the TransactionViewModelFactory.

    override fun onCreate() {
        super.onCreate()
        // Initialize notifications channel
        NotificationHelper.createNotificationChannel(this)
    }
}