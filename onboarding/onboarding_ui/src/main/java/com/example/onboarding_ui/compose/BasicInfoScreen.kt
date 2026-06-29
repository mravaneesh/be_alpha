package com.example.onboarding_ui.compose

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.designsystem.components.PactPrimaryButton
import com.example.designsystem.components.PactTextField
import com.example.designsystem.theme.PactType
import com.example.designsystem.theme.semantic
import com.example.designsystem.theme.spacing

/**
 * Onboarding step 1 — basic info. Gender/birthday open the existing bottom-sheet pickers (driven
 * by the fragment); height/weight with unit toggles are local. [onNext] reports the normalized
 * height (cm) and weight (kg); the fragment persists and advances the pager.
 */
@Composable
fun BasicInfoScreen(
    gender: String,
    birthday: String,
    onPickGender: () -> Unit,
    onPickBirthday: () -> Unit,
    onNext: (heightCm: Int, weightKg: Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    var cm by remember { mutableStateOf(true) }
    var kg by remember { mutableStateOf(true) }
    var cmValue by remember { mutableStateOf("") }
    var feet by remember { mutableStateOf("") }
    var inch by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }

    val genderOk = gender.isNotBlank() && gender != "Select"
    val bdayOk = birthday.isNotBlank() && birthday != "Select"
    val heightOk = if (cm) cmValue.isNotBlank() else feet.isNotBlank() && inch.isNotBlank()
    val weightOk = weight.isNotBlank()
    val valid = genderOk && bdayOk && heightOk && weightOk

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = MaterialTheme.spacing.screen)
            .padding(top = MaterialTheme.spacing.xl, bottom = MaterialTheme.spacing.xxl),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xl),
    ) {
        Text("A few basics", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
        Text("This helps personalize your plan.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        PickerRow("Gender", gender.ifBlank { "Select" }, onPickGender)
        PickerRow("Birthday", birthday.ifBlank { "Select" }, onPickBirthday)

        // Height
        LabeledToggle("Height", listOf("cm", "ft/in"), if (cm) 0 else 1) { cm = it == 0 }
        if (cm) {
            PactTextField(cmValue, { cmValue = it }, label = "", placeholder = "Height (cm)", keyboardType = KeyboardType.Number)
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)) {
                Box(Modifier.weight(1f)) { PactTextField(feet, { feet = it }, label = "", placeholder = "ft", keyboardType = KeyboardType.Number) }
                Box(Modifier.weight(1f)) { PactTextField(inch, { inch = it }, label = "", placeholder = "in", keyboardType = KeyboardType.Number) }
            }
        }

        // Weight
        LabeledToggle("Weight", listOf("kg", "lbs"), if (kg) 0 else 1) { kg = it == 0 }
        PactTextField(weight, { weight = it }, label = "", placeholder = if (kg) "Weight (kg)" else "Weight (lbs)", keyboardType = KeyboardType.Number)

        Spacer(Modifier.height(MaterialTheme.spacing.md))
        PactPrimaryButton(
            text = "Next",
            enabled = valid,
            onClick = {
                val heightCm = if (cm) cmValue.toIntOrNull() ?: 0
                else (((feet.toIntOrNull() ?: 0) * 12 + (inch.toIntOrNull() ?: 0)) * 2.54).toInt()
                val weightKg = if (kg) weight.toFloatOrNull() ?: 0f
                else (weight.toFloatOrNull() ?: 0f) * 0.453592f
                onNext(heightCm, weightKg)
            },
        )
    }
}

@Composable
private fun LabeledToggle(label: String, options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            options.forEachIndexed { i, opt ->
                val sel = i == selected
                Text(
                    opt,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (sel) Color(0xFF06121F) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (sel) MaterialTheme.semantic.accent else Color.Transparent)
                        .clickable { onSelect(i) }
                        .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.sm),
                )
            }
        }
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
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(BorderStroke(1.5.dp, MaterialTheme.semantic.hairlineStrong), RoundedCornerShape(14.dp))
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
