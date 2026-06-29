package com.example.utils

import android.content.Context

object Prefs {
    private const val PREFS_NAME = "bealpha_prefs"
    private const val KEY_ONBOARDING = "isOnboardingCompleted"
    private const val KEY_TOUR = "isTourCompleted"

    fun isTourCompleted(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_TOUR, false)

    fun setTourCompleted(context: Context, completed: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_TOUR, completed).apply()
    }

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

    // ---- Per-screen contextual walkthroughs (feature discovery) ----

    /** Screens that each have their own first-time walkthrough. */
    val TOUR_SCREENS = listOf("home", "habits", "stats", "community", "profile")

    private fun screenTourKey(screen: String) = "screenTour_$screen"

    fun isScreenTourSeen(context: Context, screen: String): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(screenTourKey(screen), false)

    fun setScreenTourSeen(context: Context, screen: String, seen: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(screenTourKey(screen), seen).apply()
    }

    /** "Replay walkthroughs" from Settings — clears every screen's seen flag. */
    fun resetAllScreenTours(context: Context) {
        val editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        TOUR_SCREENS.forEach { editor.remove(screenTourKey(it)) }
        editor.apply()
    }
}