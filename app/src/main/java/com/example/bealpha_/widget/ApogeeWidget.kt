package com.example.bealpha_.widget

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.bealpha_.HostActivity
import com.example.bealpha_.R

// ---- Apex palette (auto light / AMOLED-dark via values-night resources) ----------------------
private val cBg = ColorProvider(R.color.widget_bg)
private val cText = ColorProvider(R.color.widget_text)
private val cMuted = ColorProvider(R.color.widget_muted)
private val cFaint = ColorProvider(R.color.widget_faint)
private val cAccent = ColorProvider(R.color.widget_accent)
private val cStreak = ColorProvider(R.color.widget_streak)
private val cHairline = ColorProvider(R.color.widget_hairline)
private val streakTint = ColorProvider(R.color.widget_streak_tint)
private val cWhite = ColorProvider(Color.White)
private val accentSolid = Color(0xFF4D9BFF)

private val SMALL = DpSize(140.dp, 140.dp)
private val MEDIUM = DpSize(250.dp, 140.dp)
private val LARGE = DpSize(250.dp, 250.dp)

class ApogeeWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(setOf(SMALL, MEDIUM, LARGE))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = loadWidgetData(context)
        android.util.Log.d("WidgetSync", "provideGlance: state=${data.state} done=${data.doneCount}/${data.totalCount} habits=${data.habits.size}")
        provideContent { WidgetRoot(data) }
    }
}

@Composable
private fun WidgetRoot(data: WidgetData) {
    val size = LocalSize.current
    val openApp = actionStartActivity(Intent(LocalContext.current, HostActivity::class.java))
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(cBg)
            .cornerRadius(26.dp),
        contentAlignment = Alignment.Center,
    ) {
        when {
            size.width < 200.dp -> SmallLayout(data, openApp)
            size.height < 200.dp -> MediumLayout(data, openApp)
            else -> LargeLayout(data, openApp)
        }
    }
}

// ============================ SMALL (2x2) ============================
@Composable
private fun SmallLayout(data: WidgetData, openApp: Action) {
    val dark = isDark()
    when (data.state) {
        WidgetUiState.EMPTY -> EmptyCompact(openApp)
        WidgetUiState.ALL_DONE -> Column(
            modifier = GlanceModifier.fillMaxSize().padding(14.dp).clickable(openApp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RingWithCheck(progress = 1f, dark = dark, ringDp = 96.dp, checkDp = 38.dp, onOpen = openApp)
            Spacer(GlanceModifier.height(12.dp))
            Text("All done 🎉", style = TextStyle(color = cText, fontSize = 16.sp, fontWeight = FontWeight.Bold))
        }
        WidgetUiState.POPULATED -> Column(
            modifier = GlanceModifier.fillMaxSize().padding(14.dp).clickable(openApp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RingWithPercent(data.percent, dark, ringDp = 96.dp, percentSp = 24.sp, onOpen = openApp)
            Spacer(GlanceModifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${data.doneCount}/${data.totalCount}", style = mono(cMuted, 15.sp, FontWeight.Bold))
                Spacer(GlanceModifier.width(10.dp))
                StreakInline(data.streak)
            }
        }
    }
}

// ============================ MEDIUM (4x2) ============================
@Composable
private fun MediumLayout(data: WidgetData, openApp: Action) {
    val dark = isDark()
    if (data.state == WidgetUiState.EMPTY) {
        EmptyWide(openApp)
        return
    }
    Column(modifier = GlanceModifier.fillMaxSize().padding(15.dp)) {
        Row(modifier = GlanceModifier.fillMaxWidth().clickable(openApp), verticalAlignment = Alignment.CenterVertically) {
            Text("TODAY", style = mono(cFaint, 12.sp, FontWeight.Medium))
            Spacer(GlanceModifier.defaultWeight())
            StreakChip(data.streak)
        }
        Spacer(GlanceModifier.height(12.dp))
        Row(modifier = GlanceModifier.fillMaxWidth().defaultWeight(), verticalAlignment = Alignment.CenterVertically) {
            if (data.state == WidgetUiState.ALL_DONE) {
                RingWithCheck(progress = 1f, dark = dark, ringDp = 80.dp, checkDp = 32.dp, onOpen = openApp)
                Spacer(GlanceModifier.width(18.dp))
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text("All done 🎉", style = TextStyle(color = cText, fontSize = 20.sp, fontWeight = FontWeight.Bold))
                    Spacer(GlanceModifier.height(4.dp))
                    Text("${data.doneCount} of ${data.totalCount} habits done", style = TextStyle(color = cMuted, fontSize = 13.sp))
                }
            } else {
                RingWithPercent(data.percent, dark, ringDp = 80.dp, percentSp = 20.sp, onOpen = openApp)
                Spacer(GlanceModifier.width(16.dp))
                Box(modifier = GlanceModifier.defaultWeight().fillMaxHeight(), contentAlignment = Alignment.BottomCenter) {
                    LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                        items(data.habits) { h ->
                            Box(GlanceModifier.padding(vertical = 6.dp)) {
                                HabitRow(h, tileDp = 30.dp, checkDp = 30.dp)
                            }
                        }
                    }
                    Image(
                        provider = ImageProvider(RingRenderer.bottomFade(dark)),
                        contentDescription = null,
                        modifier = GlanceModifier.fillMaxWidth().height(18.dp),
                        contentScale = ContentScale.FillBounds,
                    )
                }
            }
        }
    }
}

// ============================ LARGE (4x4) ============================
@Composable
private fun LargeLayout(data: WidgetData, openApp: Action) {
    val dark = isDark()
    if (data.state == WidgetUiState.EMPTY) {
        EmptyTall(openApp)
        return
    }
    Column(modifier = GlanceModifier.fillMaxSize().padding(16.dp)) {
        // Header
        Row(modifier = GlanceModifier.fillMaxWidth().clickable(openApp), verticalAlignment = Alignment.CenterVertically) {
            BoltTile(38.dp, 20.dp)
            Spacer(GlanceModifier.width(11.dp))
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(data.greeting, style = TextStyle(color = cText, fontSize = 17.sp, fontWeight = FontWeight.Bold))
                Text(data.dateLabel, style = mono(cFaint, 12.sp, FontWeight.Medium))
            }
            StreakChip(data.streak)
        }
        Spacer(GlanceModifier.height(12.dp))
        Box(modifier = GlanceModifier.fillMaxWidth().height(1.dp).background(cHairline)) {}
        Spacer(GlanceModifier.height(11.dp))

        if (data.state == WidgetUiState.ALL_DONE) {
            Column(
                modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                RingWithCheck(progress = 1f, dark = dark, ringDp = 112.dp, checkDp = 46.dp, onOpen = openApp)
                Spacer(GlanceModifier.height(12.dp))
                Text("All done 🎉", style = TextStyle(color = cText, fontSize = 20.sp, fontWeight = FontWeight.Bold))
                Spacer(GlanceModifier.height(4.dp))
                Text("Every habit complete. Streak now ${data.streak} days.",
                    style = TextStyle(color = cMuted, fontSize = 12.sp, textAlign = TextAlign.Center))
            }
        } else {
            Box(modifier = GlanceModifier.fillMaxWidth().defaultWeight(), contentAlignment = Alignment.BottomCenter) {
                LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                    items(data.habits) { h ->
                        Box(GlanceModifier.padding(vertical = 7.dp)) {
                            HabitRow(h, tileDp = 32.dp, checkDp = 30.dp)
                        }
                    }
                }
                Image(
                    provider = ImageProvider(RingRenderer.bottomFade(dark)),
                    contentDescription = null,
                    modifier = GlanceModifier.fillMaxWidth().height(22.dp),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
        Spacer(GlanceModifier.height(12.dp))
        Box(modifier = GlanceModifier.fillMaxWidth().height(1.dp).background(cHairline)) {}
        Spacer(GlanceModifier.height(12.dp))
        WeekStrip(data.week)
    }
}

// ============================ shared pieces ============================
@Composable
private fun RingWithPercent(percent: Int, dark: Boolean, ringDp: androidx.compose.ui.unit.Dp, percentSp: androidx.compose.ui.unit.TextUnit, onOpen: Action) {
    Box(modifier = GlanceModifier.size(ringDp).clickable(onOpen), contentAlignment = Alignment.Center) {
        Image(provider = ImageProvider(RingRenderer.ring(percent / 100f, dark)), contentDescription = null, modifier = GlanceModifier.size(ringDp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$percent%", style = mono(cText, percentSp, FontWeight.Bold))
            Text("TODAY", style = TextStyle(color = cFaint, fontSize = 10.sp))
        }
    }
}

@Composable
private fun RingWithCheck(progress: Float, dark: Boolean, ringDp: androidx.compose.ui.unit.Dp, checkDp: androidx.compose.ui.unit.Dp, onOpen: Action) {
    Box(modifier = GlanceModifier.size(ringDp).clickable(onOpen), contentAlignment = Alignment.Center) {
        Image(provider = ImageProvider(RingRenderer.ring(progress, dark)), contentDescription = null, modifier = GlanceModifier.size(ringDp))
        Image(
            provider = ImageProvider(R.drawable.ic_w_check),
            contentDescription = "All done",
            modifier = GlanceModifier.size(checkDp),
            colorFilter = androidx.glance.ColorFilter.tint(cAccent),
        )
    }
}

@Composable
private fun HabitRow(h: WidgetHabit, tileDp: androidx.compose.ui.unit.Dp, checkDp: androidx.compose.ui.unit.Dp) {
    val (color, iconRes) = categoryStyle(h.category)
    // Tapping a habit opens the app on the Habits screen, scrolled to and highlighting this habit.
    val focus = actionStartActivity(
        Intent(LocalContext.current, HostActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("apogee://focusHabit/${h.id}")
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
    )
    Row(modifier = GlanceModifier.fillMaxWidth().clickable(focus).padding(end = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = GlanceModifier.size(tileDp).cornerRadius(8.dp).background(ColorProvider(color.copy(alpha = 0.16f))),
            contentAlignment = Alignment.Center,
        ) {
            Image(provider = ImageProvider(iconRes), contentDescription = null,
                modifier = GlanceModifier.size(tileDp * 0.55f), colorFilter = androidx.glance.ColorFilter.tint(ColorProvider(color)))
        }
        Spacer(GlanceModifier.width(12.dp))
        Text(
            text = h.title,
            maxLines = 1,
            style = TextStyle(
                color = if (h.done) cMuted else cText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textDecoration = if (h.done) TextDecoration.LineThrough else TextDecoration.None,
            ),
            modifier = GlanceModifier.defaultWeight(),
        )
        Spacer(GlanceModifier.width(12.dp))
        StatusGlyph(h.done, checkDp)
    }
}

/** Completed / pending status indicator (not a checkbox — the row opens the app). */
@Composable
private fun StatusGlyph(done: Boolean, sizeDp: androidx.compose.ui.unit.Dp) {
    Box(modifier = GlanceModifier.size(sizeDp), contentAlignment = Alignment.Center) {
        if (done) {
            Image(provider = ImageProvider(R.drawable.ic_w_check), contentDescription = "Completed",
                modifier = GlanceModifier.size(sizeDp * 0.7f), colorFilter = androidx.glance.ColorFilter.tint(cAccent))
        } else {
            Image(provider = ImageProvider(R.drawable.ic_w_ring), contentDescription = "Pending",
                modifier = GlanceModifier.size(sizeDp * 0.7f), colorFilter = androidx.glance.ColorFilter.tint(cFaint))
        }
    }
}

@Composable
private fun StreakChip(streak: Int) {
    Row(
        modifier = GlanceModifier.cornerRadius(12.dp).background(streakTint).padding(horizontal = 11.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(provider = ImageProvider(R.drawable.ic_w_fire), contentDescription = null,
            modifier = GlanceModifier.size(14.dp), colorFilter = androidx.glance.ColorFilter.tint(cStreak))
        Spacer(GlanceModifier.width(5.dp))
        Text("$streak", style = mono(cStreak, 13.sp, FontWeight.Bold))
    }
}

@Composable
private fun StreakInline(streak: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(provider = ImageProvider(R.drawable.ic_w_fire), contentDescription = null,
            modifier = GlanceModifier.size(16.dp), colorFilter = androidx.glance.ColorFilter.tint(cStreak))
        Spacer(GlanceModifier.width(5.dp))
        Text("$streak", style = mono(cStreak, 15.sp, FontWeight.Bold))
    }
}

@Composable
private fun WeekStrip(week: List<WeekBar>) {
    Row(modifier = GlanceModifier.fillMaxWidth().height(36.dp), verticalAlignment = Alignment.Bottom) {
        week.forEach { bar ->
            Column(
                modifier = GlanceModifier.defaultWeight().padding(horizontal = 1.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.Bottom,
            ) {
                val h = (3 + (bar.ratio * 16)).dp
                Box(
                    modifier = GlanceModifier.fillMaxWidth().height(h).cornerRadius(4.dp)
                        .background(if (bar.isToday) cAccent else ColorProvider(accentSolid.copy(alpha = 0.22f))),
                ) {}
                Spacer(GlanceModifier.height(4.dp))
                Text(bar.label, style = mono(if (bar.isToday) cAccent else cFaint, 9.sp, FontWeight.Medium))
            }
        }
    }
}

@Composable
private fun BoltTile(boxDp: androidx.compose.ui.unit.Dp, iconDp: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = GlanceModifier.size(boxDp).cornerRadius(boxDp / 3).background(ColorProvider(accentSolid)),
        contentAlignment = Alignment.Center,
    ) {
        Image(provider = ImageProvider(R.drawable.ic_w_bolt), contentDescription = null,
            modifier = GlanceModifier.size(iconDp), colorFilter = androidx.glance.ColorFilter.tint(cWhite))
    }
}

// ---- Empty states ----
@Composable
private fun EmptyCompact(openApp: Action) {
    Column(
        modifier = GlanceModifier.fillMaxSize().padding(16.dp).clickable(openApp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BoltTile(46.dp, 25.dp)
        Spacer(GlanceModifier.height(11.dp))
        Text("Add a habit", style = TextStyle(color = cText, fontSize = 13.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center))
        Text("in Apogee", style = TextStyle(color = cFaint, fontSize = 11.sp, textAlign = TextAlign.Center))
    }
}

@Composable
private fun EmptyWide(openApp: Action) {
    Row(modifier = GlanceModifier.fillMaxSize().padding(18.dp).clickable(openApp), verticalAlignment = Alignment.CenterVertically) {
        BoltTile(54.dp, 29.dp)
        Spacer(GlanceModifier.width(16.dp))
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text("No habits yet", style = TextStyle(color = cText, fontSize = 15.sp, fontWeight = FontWeight.Bold))
            Spacer(GlanceModifier.height(3.dp))
            Text("Add a habit in Apogee to start your streak.", style = TextStyle(color = cMuted, fontSize = 12.sp))
            Spacer(GlanceModifier.height(10.dp))
            AddHabitPill()
        }
    }
}

@Composable
private fun EmptyTall(openApp: Action) {
    Column(
        modifier = GlanceModifier.fillMaxSize().padding(18.dp).clickable(openApp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BoltTile(64.dp, 34.dp)
        Spacer(GlanceModifier.height(14.dp))
        Text("No habits yet", style = TextStyle(color = cText, fontSize = 17.sp, fontWeight = FontWeight.Bold))
        Spacer(GlanceModifier.height(6.dp))
        Text("Add your first habit in Apogee and it will show up here.",
            style = TextStyle(color = cMuted, fontSize = 13.sp, textAlign = TextAlign.Center))
        Spacer(GlanceModifier.height(14.dp))
        AddHabitPill()
    }
}

@Composable
private fun AddHabitPill() {
    Row(
        modifier = GlanceModifier.cornerRadius(12.dp).background(ColorProvider(accentSolid))
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .clickable(actionStartActivity(Intent(LocalContext.current, HostActivity::class.java))),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(provider = ImageProvider(R.drawable.ic_w_plus), contentDescription = null,
            modifier = GlanceModifier.size(13.dp), colorFilter = androidx.glance.ColorFilter.tint(cWhite))
        Spacer(GlanceModifier.width(6.dp))
        Text("Add habit", style = TextStyle(color = cWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold))
    }
}

// ---- helpers ----
private fun mono(color: ColorProvider, size: androidx.compose.ui.unit.TextUnit, weight: FontWeight) =
    TextStyle(color = color, fontSize = size, fontWeight = weight, fontFamily = FontFamily.Monospace)

@Composable
private fun isDark(): Boolean {
    val cfg = LocalContext.current.resources.configuration
    return (cfg.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
}

private fun categoryStyle(category: String): Pair<Color, Int> {
    val c = category.lowercase()
    return when {
        "fit" in c || "work" in c || "gym" in c -> Color(0xFF4D9BFF) to R.drawable.ic_w_dumbbell
        "read" in c || "learn" in c -> Color(0xFF8B7BFF) to R.drawable.ic_w_book
        "mind" in c || "medit" in c -> Color(0xFF3DDC97) to R.drawable.ic_w_spark
        "health" in c || "water" in c || "drink" in c -> Color(0xFF36C6E0) to R.drawable.ic_w_drop
        "disciplin" in c || "sugar" in c || "no " in c -> Color(0xFFFF7A45) to R.drawable.ic_w_block
        "sleep" in c || "recover" in c -> Color(0xFFFFB259) to R.drawable.ic_w_moon
        else -> Color(0xFF4D9BFF) to R.drawable.ic_w_bolt
    }
}
