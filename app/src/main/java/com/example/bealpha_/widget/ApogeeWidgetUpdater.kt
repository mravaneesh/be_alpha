package com.example.bealpha_.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Refreshes every placed Apogee widget after the app mutates habit data. Fire-and-forget. */
object ApogeeWidgetUpdater {
    fun refresh(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching { ApogeeWidget().updateAll(context.applicationContext) }
        }
    }
}
