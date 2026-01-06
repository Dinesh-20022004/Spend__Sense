package com.example.spendsense

import android.content.Context
import android.content.SharedPreferences

object UserSessionManager {

    private const val PREFS_NAME = "SpendSense_App"
    private const val KEY_LAST_LOGGED_IN_EMAIL = "last_logged_in_email"
    private const val KEY_USER_NAME = "current_user_name" // <-- NEW KEY

    private fun getAppPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getUserPreferences(context: Context): SharedPreferences? {
        val email = getLoggedInEmail(context) ?: return null
        val userPrefsName = "UserData_${email}"
        return context.getSharedPreferences(userPrefsName, Context.MODE_PRIVATE)
    }

    fun setLoggedInEmail(context: Context, email: String) {
        getAppPreferences(context).edit().putString(KEY_LAST_LOGGED_IN_EMAIL, email).apply()
    }

    fun getLoggedInEmail(context: Context): String? {
        return getAppPreferences(context).getString(KEY_LAST_LOGGED_IN_EMAIL, null)
    }

    // --- NEW FUNCTIONS FOR NAME ---
    fun saveUserName(context: Context, name: String) {
        getAppPreferences(context).edit().putString(KEY_USER_NAME, name).apply()
    }

    fun getUserName(context: Context): String? {
        return getAppPreferences(context).getString(KEY_USER_NAME, "User")
    }
    // -----------------------------

    fun clearSession(context: Context) {
        getAppPreferences(context).edit().clear().apply()
    }
}