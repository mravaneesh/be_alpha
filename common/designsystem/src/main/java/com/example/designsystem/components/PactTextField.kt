package com.example.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.designsystem.theme.semantic
import com.example.designsystem.theme.spacing

/**
 * Apex text field: a small uppercase-ish label above a rounded input (radius 16, surface fill,
 * 1.5dp border). The border turns accent + grows a soft glow ring on focus, matching the
 * redesign's auth inputs. Optional [leadingIcon], [trailing] slot (e.g. a validity indicator),
 * and password masking with a built-in show/hide toggle.
 */
@Composable
fun PactTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    leadingIcon: ImageVector? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    var focused by remember { mutableStateOf(false) }
    var reveal by remember { mutableStateOf(false) }
    val accent = MaterialTheme.semantic.accent

    val borderColor by animateColorAsState(
        if (focused) accent else MaterialTheme.semantic.hairlineStrong,
        label = "fieldBorder",
    )
    val glow = if (focused) accent.copy(alpha = 0.14f) else androidx.compose.ui.graphics.Color.Transparent

    Column(modifier = modifier.fillMaxWidth()) {
        if (label.isNotEmpty()) {
            Text(
                label.uppercase(),
                style = com.example.designsystem.theme.PactType.eyebrow,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(MaterialTheme.spacing.sm))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(glow)
                .padding(3.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.5.dp, borderColor, MaterialTheme.shapes.small)
                .padding(horizontal = MaterialTheme.spacing.xl),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingIcon != null) {
                Icon(leadingIcon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(MaterialTheme.spacing.md))
            }
            Box(Modifier.weight(1f)) {
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    Text(placeholder, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(accent),
                    visualTransformation = if (isPassword && !reveal) PasswordVisualTransformation() else VisualTransformation.None,
                    keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else keyboardType),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focused = it.isFocused },
                )
            }
            if (isPassword) {
                Icon(
                    imageVector = if (reveal) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = if (reveal) "Hide password" else "Show password",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { reveal = !reveal },
                )
            }
            if (trailing != null) {
                Spacer(Modifier.width(MaterialTheme.spacing.sm))
                trailing()
            }
        }
    }
}
