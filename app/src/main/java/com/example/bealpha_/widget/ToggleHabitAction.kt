package com.example.bealpha_.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback

/** Marks one habit complete from the widget, then refreshes the widget surface. */
class ToggleHabitAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val habitId = parameters[habitIdKey] ?: return
        val goal = findHabit(context, habitId) ?: return
        toggleHabit(context, goal)
        ApogeeWidget().update(context, glanceId)
    }

    companion object {
        val habitIdKey = ActionParameters.Key<String>("habitId")
    }
}
