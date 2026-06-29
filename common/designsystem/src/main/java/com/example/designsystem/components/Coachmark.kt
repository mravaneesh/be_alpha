package com.example.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.designsystem.theme.semantic
import com.example.designsystem.theme.spacing
import kotlin.math.roundToInt

/** One stop on a screen's first-time walkthrough: the anchor [key] plus its tooltip copy. */
data class CoachmarkStep(
    val key: String,
    val title: String,
    val message: String,
)

/** Holds the measured root-space bounds of every tagged anchor for the active host. */
class CoachmarkState internal constructor() {
    internal val anchors = mutableStateMapOf<String, Rect>()
    internal var hostOrigin: Offset = Offset.Zero
}

// onGloballyPositioned can't read a CompositionLocal, so the active host publishes itself here for
// the duration of its composition. Only one walkthrough runs at a time (the top-most screen).
private var activeSink: CoachmarkState? = null

@Suppress("unused")
private val LocalCoachmark = compositionLocalOf<CoachmarkState?> { null }

/**
 * Tag a composable as a walkthrough anchor. [CoachmarkHost] spotlights it when its step is active.
 * A no-op when no tour is running, so it's safe to leave on permanently.
 */
fun Modifier.coachmarkTarget(key: String): Modifier = this.onGloballyPositioned { coords ->
    activeSink?.anchors?.set(
        key,
        Rect(coords.positionInRoot(), Size(coords.size.width.toFloat(), coords.size.height.toFloat())),
    )
}

/**
 * Wraps a screen and runs its contextual walkthrough the first time it's seen.
 *
 * @param enabled true (with non-empty [steps]) shows the overlay; flip via persisted "seen" state.
 * @param onFinish fired once when the user finishes or skips — persist "seen" here.
 */
@Composable
fun CoachmarkHost(
    steps: List<CoachmarkStep>,
    enabled: Boolean,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val state = remember { CoachmarkState() }
    var running by remember { mutableStateOf(enabled) }
    var index by remember { mutableIntStateOf(0) }

    DisposableEffect(Unit) {
        activeSink = state
        onDispose { if (activeSink === state) activeSink = null }
    }

    Box(
        modifier
            .fillMaxSize()
            .onGloballyPositioned { state.hostOrigin = it.positionInRoot() },
    ) {
        content()

        val step = steps.getOrNull(index)
        val target = step?.let { state.anchors[it.key] }
        // Only render once the anchor is measured, so the spotlight never flashes in the corner.
        val show = running && enabled && step != null && target != null
        AnimatedVisibility(visible = show, enter = fadeIn(), exit = fadeOut()) {
            if (step != null && target != null) {
                CoachmarkOverlay(
                    step = step,
                    index = index,
                    count = steps.size,
                    target = target.translate(-state.hostOrigin),
                    onBack = { if (index > 0) index-- },
                    onNext = { if (index < steps.lastIndex) index++ else { running = false; onFinish() } },
                    onSkip = { running = false; onFinish() },
                )
            }
        }
    }
}

private fun Rect.translate(o: Offset) = Rect(left + o.x, top + o.y, right + o.x, bottom + o.y)

@Composable
private fun CoachmarkOverlay(
    step: CoachmarkStep,
    index: Int,
    count: Int,
    target: Rect,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit,
) {
    val density = LocalDensity.current
    val padPx = with(density) { 8.dp.toPx() }
    val radiusPx = with(density) { 18.dp.toPx() }
    val accent = MaterialTheme.semantic.accent

    val spot = Rect(target.left - padPx, target.top - padPx, target.right + padPx, target.bottom + padPx)

    Box(Modifier.fillMaxSize()) {
        // (1) Dim scrim with a rounded cutout over the highlighted element. Tapping it advances.
        Canvas(
            Modifier
                .fillMaxSize()
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onNext),
        ) {
            val scrim = Path().apply {
                fillType = PathFillType.EvenOdd
                addRect(Rect(Offset.Zero, size))
                addRoundRect(RoundRect(spot, CornerRadius(radiusPx, radiusPx)))
            }
            drawIntoCanvas { it.drawPath(scrim, Paint().apply { color = Color.Black.copy(alpha = 0.72f) }) }
        }

        // (2) Accent spotlight ring around the element.
        Canvas(Modifier.fillMaxSize()) {
            drawIntoCanvas {
                val ring = Path().apply { addRoundRect(RoundRect(spot, CornerRadius(radiusPx, radiusPx))) }
                it.drawPath(
                    ring,
                    Paint().apply {
                        color = accent.copy(alpha = 0.9f)
                        style = PaintingStyle.Stroke
                        strokeWidth = with(density) { 2.dp.toPx() }
                    },
                )
            }
        }

        // (3) Floating tooltip card — below the anchor if it's in the top half, else above it.
        TooltipCard(step, index, count, spot, onBack, onNext, onSkip)
    }
}

@Composable
private fun TooltipCard(
    step: CoachmarkStep,
    index: Int,
    count: Int,
    anchor: Rect,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit,
) {
    val density = LocalDensity.current
    val gapPx = with(density) { 14.dp.toPx() }
    val minTopPx = with(density) { 24.dp.toPx() }
    val first = index == 0
    val last = index == count - 1
    var cardH by remember { mutableIntStateOf(0) }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val maxHpx = with(density) { maxHeight.toPx() }
        val below = anchor.center.y < maxHpx / 2f
        val y = (if (below) anchor.bottom + gapPx else anchor.top - gapPx - cardH)
            .roundToInt().coerceAtLeast(minTopPx.roundToInt())

        androidx.compose.runtime.key(index) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + scaleIn(initialScale = 0.92f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
                exit = fadeOut() + scaleOut(targetScale = 0.92f),
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .offset { IntOffset(0, y) }
                        .onSizeChanged { cardH = it.height }
                        .clip(RoundedCornerShape(22.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(BorderStroke(1.dp, MaterialTheme.semantic.hairlineStrong), RoundedCornerShape(22.dp))
                        .padding(MaterialTheme.spacing.xxl),
                ) {
                    Text(step.title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.height(MaterialTheme.spacing.sm))
                    Text(step.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(MaterialTheme.spacing.xl))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (first) TextAction("Skip", muted = true, onClick = onSkip)
                        else TextAction("Back", muted = true, onClick = onBack)
                        Spacer(Modifier.width(MaterialTheme.spacing.md))
                        ProgressDots(index, count, Modifier.weight(1f))
                        Spacer(Modifier.width(MaterialTheme.spacing.md))
                        PrimaryAction(if (last) "Got it" else "Next", onClick = onNext)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressDots(index: Int, count: Int, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(count) { i ->
            val active = i == index
            Box(
                Modifier
                    .height(6.dp)
                    .width(if (active) 18.dp else 6.dp)
                    .clip(CircleShape)
                    .background(if (active) MaterialTheme.semantic.accent else MaterialTheme.semantic.hairlineStrong),
            )
        }
    }
}

@Composable
private fun TextAction(label: String, muted: Boolean, onClick: () -> Unit) {
    Text(
        label,
        style = MaterialTheme.typography.labelLarge,
        color = if (muted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.semantic.accent,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.sm),
    )
}

@Composable
private fun PrimaryAction(label: String, onClick: () -> Unit) {
    Text(
        label,
        style = MaterialTheme.typography.labelLarge,
        color = Color(0xFF06121F),
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.semantic.accent)
            .clickable(onClick = onClick)
            .padding(horizontal = MaterialTheme.spacing.xl, vertical = MaterialTheme.spacing.md),
    )
}
