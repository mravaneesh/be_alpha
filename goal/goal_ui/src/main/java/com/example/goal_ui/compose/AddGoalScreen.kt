package com.example.goal_ui.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.designsystem.components.PactPrimaryButton
import com.example.designsystem.components.PactTextField
import com.example.designsystem.theme.PactType
import com.example.designsystem.theme.semantic
import com.example.designsystem.theme.spacing

/** ARGB swatches offered for a habit. Index is stored on the Goal as its color. */
val HabitPalette = listOf(
    0xFF4D9BFF, 0xFF8B7BFF, 0xFF3DDC97, 0xFF36C6E0, 0xFFFF9F45, 0xFFFF5D5D, 0xFFFFB259,
).map { it.toInt() }

private val dayLabels = listOf("S", "M", "T", "W", "T", "F", "S") // index 0 = Sunday

/**
 * Create / edit habit form in the Apex style. Holds field state; the fragment supplies the
 * save/update + time-picker callbacks so the existing Firestore logic is preserved.
 */
@Composable
fun AddGoalScreen(
    isEdit: Boolean,
    initialTitle: String,
    initialDescription: String,
    initialDays: Set<Int>,
    initialColor: Int,
    initialReminder: String,
    onPickTime: (current: String, onPicked: (String) -> Unit) -> Unit,
    onSave: (title: String, description: String, days: List<Int>, color: Int, reminder: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }
    val days = remember { mutableStateListOf<Int>().apply { addAll(if (initialDays.isEmpty()) (0..6).toList() else initialDays) } }
    var color by remember { mutableStateOf(if (HabitPalette.contains(initialColor)) initialColor else HabitPalette.first()) }
    var reminderOn by remember { mutableStateOf(initialReminder.isNotBlank()) }
    var reminderTime by remember { mutableStateOf(initialReminder) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = MaterialTheme.spacing.screen)
            .padding(bottom = MaterialTheme.spacing.xxl),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.size(MaterialTheme.spacing.sm))
            Text(if (isEdit) "Edit habit" else "New habit", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
        }
        Spacer(Modifier.height(MaterialTheme.spacing.xl))

        PactTextField(value = title, onValueChange = { title = it }, label = "Habit name", placeholder = "Morning workout")
        Spacer(Modifier.height(MaterialTheme.spacing.xl))
        PactTextField(value = description, onValueChange = { description = it }, label = "Description", placeholder = "Optional")
        Spacer(Modifier.height(MaterialTheme.spacing.xxl))

        Text("REPEAT", style = PactType.eyebrow, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(MaterialTheme.spacing.md))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            dayLabels.forEachIndexed { i, label ->
                DayChip(label = label, selected = days.contains(i)) {
                    if (days.contains(i)) days.remove(i) else days.add(i)
                }
            }
        }
        Spacer(Modifier.height(MaterialTheme.spacing.xxl))

        Text("COLOR", style = PactType.eyebrow, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(MaterialTheme.spacing.md))
        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)) {
            HabitPalette.forEach { c ->
                ColorSwatch(color = Color(c), selected = c == color) { color = c }
            }
        }
        Spacer(Modifier.height(MaterialTheme.spacing.xxl))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Reminder", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    if (reminderOn && reminderTime.isNotBlank()) reminderTime else "Off",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable(enabled = reminderOn) {
                        onPickTime(reminderTime) { reminderTime = it }
                    },
                )
            }
            Switch(
                checked = reminderOn,
                onCheckedChange = {
                    reminderOn = it
                    if (it && reminderTime.isBlank()) onPickTime("") { t -> reminderTime = t }
                },
            )
        }

        Spacer(Modifier.height(MaterialTheme.spacing.xxl))
        PactPrimaryButton(
            text = if (isEdit) "Update habit" else "Add habit",
            enabled = title.isNotBlank() && days.isNotEmpty(),
            onClick = {
                onSave(title.trim(), description.trim(), days.sorted(), color, if (reminderOn) reminderTime else "")
            },
        )
    }
}

@Composable
private fun DayChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (selected) MaterialTheme.semantic.accent else MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) Color(0xFF06121F) else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ColorSwatch(color: Color, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                BorderStroke(if (selected) 2.dp else 0.dp, MaterialTheme.colorScheme.onBackground),
                CircleShape,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) Icon(Icons.Filled.Check, contentDescription = "Selected", tint = Color(0xFF06121F), modifier = Modifier.size(18.dp))
    }
}
