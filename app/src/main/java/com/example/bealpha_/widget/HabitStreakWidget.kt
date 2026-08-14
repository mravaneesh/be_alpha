package com.example.bealpha_.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.example.bealpha_.HostActivity
import com.example.bealpha_.R
import com.example.goal_domain.usecase.HabitCompletion

/** Which habit a given widget instance tracks; written by [HabitStreakWidgetConfigActivity]. */
val habitStreakIdKey = stringPreferencesKey("habitId")

private val cBg = ColorProvider(R.color.widget_bg)
private val cText = ColorProvider(R.color.widget_text)
private val cFaint = ColorProvider(R.color.widget_faint)
private val cStreak = ColorProvider(R.color.widget_streak)

/** Fixed 2x2 widget showing one habit's streak; which habit is chosen per-instance at placement. */
class HabitStreakWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = getAppWidgetState(context, PreferencesGlanceStateDefinition, id)
        val habitId = prefs[habitStreakIdKey]
        val habit = habitId?.let { loadHabitStreak(context, it) }
        provideContent { HabitStreakContent(habit) }
    }
}

private data class HabitStreak(val id: String, val title: String, val streak: Int, val done: Boolean)

private suspend fun loadHabitStreak(context: Context, habitId: String): HabitStreak? {
    val goal = findHabit(context, habitId) ?: return null
    return HabitStreak(
        id = goal.id,
        title = goal.title,
        streak = HabitCompletion.habitStreak(goal),
        done = HabitCompletion.isDoneOn(goal),
    )
}

@Composable
private fun HabitStreakContent(habit: HabitStreak?) {
    val openIntent = Intent(LocalContext.current, HostActivity::class.java).apply {
        if (habit != null) {
            action = Intent.ACTION_VIEW
            data = Uri.parse("apogee://focusHabit/${habit.id}")
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
    }
    val openApp = actionStartActivity(openIntent)
    Box(
        modifier = GlanceModifier.fillMaxSize().background(cBg).cornerRadius(16.dp).clickable(openApp),
        contentAlignment = Alignment.Center,
    ) {
        if (habit == null) {
            Image(provider = ImageProvider(R.drawable.ic_w_fire), contentDescription = "Pick a habit",
                modifier = GlanceModifier.size(20.dp), colorFilter = androidx.glance.ColorFilter.tint(cFaint))
        } else {
            Column(
                modifier = GlanceModifier.padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(provider = ImageProvider(R.drawable.ic_w_fire), contentDescription = null,
                        modifier = GlanceModifier.size(12.dp),
                        colorFilter = androidx.glance.ColorFilter.tint(if (habit.done) cStreak else cFaint))
                    Spacer(GlanceModifier.width(3.dp))
                    Text(
                        text = "${habit.streak}",
                        style = TextStyle(color = if (habit.done) cStreak else cText, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
                    )
                }
                Text(
                    text = habit.title.ifBlank { "Habit" },
                    maxLines = 1,
                    style = TextStyle(color = cFaint, fontSize = 9.sp, textAlign = TextAlign.Center),
                )
            }
        }
    }
}
