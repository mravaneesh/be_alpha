package com.example.bealpha_.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.lifecycle.lifecycleScope
import com.example.designsystem.theme.PactTheme
import com.example.goal_domain.model.Goal
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Shown when a Habit Streak widget is placed: pick which habit this instance should track. */
class HabitStreakWidgetConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            var habits by remember { mutableStateOf<List<Goal>?>(null) }
            LaunchedEffect(Unit) {
                habits = runCatching {
                    EntryPointAccessors.fromApplication(applicationContext, WidgetEntryPoint::class.java)
                        .goalRepository().observeGoals("Habit").first()
                }.getOrDefault(emptyList())
            }
            PactTheme {
                Column(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(top = 32.dp),
                ) {
                    Text(
                        "Choose a habit",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                    androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 8.dp))
                    val list = habits
                    if (list == null || list.isEmpty()) {
                        Text(
                            if (list == null) "Loading..." else "Add a habit in Apogee first.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(20.dp),
                        )
                    } else {
                        LazyColumn {
                            items(list) { goal ->
                                Text(
                                    goal.title.ifBlank { "Habit" },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { pickHabit(goal) }
                                        .padding(horizontal = 20.dp, vertical = 16.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun pickHabit(goal: Goal) {
        lifecycleScope.launch {
            val glanceId = GlanceAppWidgetManager(this@HabitStreakWidgetConfigActivity).getGlanceIdBy(appWidgetId)
            updateAppWidgetState(this@HabitStreakWidgetConfigActivity, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                prefs.toMutablePreferences().apply { this[habitStreakIdKey] = goal.id }
            }
            HabitStreakWidget().update(this@HabitStreakWidgetConfigActivity, glanceId)
            setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))
            finish()
        }
    }
}
