package com.example.utils

import android.content.Context

object Prefs {
    private const val PREFS_NAME = "bealpha_prefs"
    private const val KEY_ONBOARDING = "isOnboardingCompleted"

    fun setOnboardingCompleted(context: Context, completed: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ONBOARDING, completed)
            .apply()
    }

    fun isOnboardingCompleted(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_ONBOARDING, false)
    }

    fun isKeyExists(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .contains(KEY_ONBOARDING)
    }
}