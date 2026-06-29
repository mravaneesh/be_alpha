package com.example.profile_ui.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.designsystem.components.PactPrimaryButton
import com.example.designsystem.components.PactTextField
import com.example.designsystem.theme.PactType
import com.example.designsystem.theme.semantic
import com.example.designsystem.theme.spacing
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private val GENDERS = listOf("Male", "Female", "Other")

/**
 * Edit profile form. Gender & birthday use themed Compose pickers (bottom sheet + Material3
 * date picker) so they match the Apex design; the fragment owns the Firestore update.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    initialName: String,
    initialUsername: String,
    initialBio: String,
    initialGender: String,
    initialBirthday: String,
    onSave: (name: String, username: String, bio: String, gender: String, birthday: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by remember { mutableStateOf(initialName) }
    var username by remember { mutableStateOf(initialUsername) }
    var bio by remember { mutableStateOf(initialBio) }
    var gender by remember { mutableStateOf(initialGender) }
    var birthday by remember { mutableStateOf(initialBirthday) }

    var showGender by remember { mutableStateOf(false) }
    var showDate by remember { mutableStateOf(false) }

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
            Spacer(Modifier.height(MaterialTheme.spacing.sm))
            Text("Edit profile", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
        }
        Spacer(Modifier.height(MaterialTheme.spacing.xl))

        PactTextField(value = name, onValueChange = { name = it }, label = "Name", placeholder = "Your name")
        Spacer(Modifier.height(MaterialTheme.spacing.xl))
        PactTextField(value = username, onValueChange = { username = it }, label = "Username", placeholder = "your_username")
        Spacer(Modifier.height(MaterialTheme.spacing.xl))
        PactTextField(value = bio, onValueChange = { bio = it }, label = "Bio", placeholder = "A short bio")
        Spacer(Modifier.height(MaterialTheme.spacing.xl))

        PickerRow(label = "Gender", value = gender.ifBlank { "Select" }, onClick = { showGender = true })
        Spacer(Modifier.height(MaterialTheme.spacing.md))
        PickerRow(label = "Birthday", value = birthday.ifBlank { "Select" }, onClick = { showDate = true })

        Spacer(Modifier.height(MaterialTheme.spacing.xxl))
        PactPrimaryButton(text = "Save changes", onClick = { onSave(name.trim(), username.trim(), bio.trim(), gender, birthday) })
    }

    if (showGender) {
        GenderSheet(current = gender, onPick = { gender = it; showGender = false }, onDismiss = { showGender = false })
    }
    if (showDate) {
        BirthdayDialog(current = birthday, onPick = { birthday = it; showDate = false }, onDismiss = { showDate = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GenderSheet(current: String, onPick: (String) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxWidth().padding(MaterialTheme.spacing.screen).padding(bottom = MaterialTheme.spacing.xxl)) {
            Text("Select gender", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(MaterialTheme.spacing.md))
            GENDERS.forEach { g ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .clickable { onPick(g) }
                        .padding(vertical = MaterialTheme.spacing.lg, horizontal = MaterialTheme.spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(g, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                    if (g == current) Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.semantic.accent)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BirthdayDialog(current: String, onPick: (String) -> Unit, onDismiss: () -> Unit) {
    val initialMillis = runCatching {
        val (d, m, y) = current.split("/").map { it.toInt() }
        LocalDate.of(y, m, d).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    }.getOrNull()

    val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { millis ->
                    val date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                    onPick("${date.dayOfMonth}/${date.monthValue}/${date.year}")
                }
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    ) {
        DatePicker(state = state, title = null)
    }
}

@Composable
private fun PickerRow(label: String, value: String, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Text(label.uppercase(), style = PactType.eyebrow, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(MaterialTheme.spacing.sm))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surface)
                .border(BorderStroke(1.5.dp, MaterialTheme.semantic.hairlineStrong), MaterialTheme.shapes.small)
                .clickable(onClick = onClick)
                .padding(horizontal = MaterialTheme.spacing.xl),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
