package com.example.social.ui.view

/**
 * Implemented by the host Activity so the Community fragment can hide the app's floating bottom bar
 * while the user is on an internal sub-screen (leaderboard / add-friends / challenges / detail /
 * create), and show it again on the Community home.
 */
interface CommunityNavHost {
    fun onCommunitySubScreen(active: Boolean)
}
