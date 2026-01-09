package com.example.spendsense

import android.app.Application

class SpendSenseApplication : Application() {

    // The Application class no longer initializes the database or repository properties.
    // These are now created on-demand within the ViewModelFactory classes
    // (TransactionViewModelFactory, BudgetViewModelFactory, AuthViewModelFactory)
    // to ensure the correct user-specific database is accessed.

    override fun onCreate() {
        super.onCreate()

        // Create the notification channel when the app starts
        NotificationHelper.createNotificationChannel(this)
    }
}