package com.example.bealpha_.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/** Hosts the Apogee home-screen widget; the system delivers update/resize broadcasts here. */
class ApogeeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ApogeeWidget()
}
