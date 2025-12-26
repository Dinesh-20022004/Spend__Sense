package com.example.spendsense

import android.content.Context
import android.content.SharedPreferences

object UserSessionManager {

    private const val PREFS_NAME = "SpendSense_App"
    private const val KEY_LAST_LOGGED_IN_EMAIL = "last_logged_in_email"

    // This gets the main app preferences (to store the last logged-in user)
    private fun getAppPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // This gets the preferences file SPECIFIC to the currently logged-in user
    fun getUserPreferences(context: Context): SharedPreferences? {
        val email = getLoggedInEmail(context) ?: return null
        // Create a unique file name for each user, e.g., "UserData_test@example.com"
        val userPrefsName = "UserData_${email}"
        return context.getSharedPreferences(userPrefsName, Context.MODE_PRIVATE)
    }

    fun setLoggedInEmail(context: Context, email: String) {
        getAppPreferences(context).edit().putString(KEY_LAST_LOGGED_IN_EMAIL, email).apply()
    }

    fun getLoggedInEmail(context: Context): String? {
        return getAppPreferences(context).getString(KEY_LAST_LOGGED_IN_EMAIL, null)
    }

    fun clearSession(context: Context) {
        getAppPreferences(context).edit().remove(KEY_LAST_LOGGED_IN_EMAIL).apply()
    }
}