package com.example.spendsense

import android.content.Context

object CurrencyHelper {
    private const val PREFS_NAME = "AppPrefs"
    private const val KEY_CURRENCY = "currency_symbol"

    // Default to Rupee if nothing is saved
    fun getCurrencySymbol(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_CURRENCY, "₹") ?: "₹"
    }

    fun setCurrencySymbol(context: Context, symbol: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CURRENCY, symbol).apply()
    }
}