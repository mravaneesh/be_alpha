package com.example.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.designsystem.theme.semantic

/**
 * Circular progress ring (Apex hero). Gradient stroke, round caps, sweeps from 12 o'clock; the
 * sweep animates on data change. [content] is centered (e.g. the percentage label).
 */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 11.dp,
    trackColor: Color = MaterialTheme.semantic.hairlineStrong,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 700, easing = PactEasing),
        label = "ringSweep",
    )
    val brush = Brush.linearGradient(
        listOf(MaterialTheme.semantic.accentBright, MaterialTheme.semantic.accent),
    )
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
            val inset = strokeWidth.toPx() / 2f
            val arcSize = Size(size.width - strokeWidth.toPx(), size.height - strokeWidth.toPx())
            val topLeft = Offset(inset, inset)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )
            drawArc(
                brush = brush,
                startAngle = -90f,
                sweepAngle = 360f * animated,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )
        }
        content()
    }
}

/**
 * 7-bar weekly summary. [values] are 0..1 heights; the [todayIndex] bar gets the accent gradient,
 * the rest a muted accent tint.
 */
@Composable
fun WeeklyBars(
    values: List<Float>,
    modifier: Modifier = Modifier,
    todayIndex: Int = values.lastIndex,
    barHeight: Dp = 96.dp,
) {
    val accent = MaterialTheme.semantic.accent
    val accentBright = MaterialTheme.semantic.accentBright
    val muted = accent.copy(alpha = 0.28f)
    Row(
        modifier = modifier.height(barHeight),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        values.forEachIndexed { i, v ->
            val frac by animateFloatAsState(
                targetValue = v.coerceIn(0.04f, 1f),
                animationSpec = tween(durationMillis = 700, easing = PactEasing),
                label = "bar$i",
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(frac)
                    .let {
                        if (i == todayIndex) it.background(
                            brush = Brush.verticalGradient(listOf(accentBright, accent)),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
                        ) else it.background(
                            color = muted,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
                        )
                    },
            )
        }
    }
}

/**
 * Monthly trend: an accent line with a gradient area fill fading to transparent. [points] are
 * 0..1 values left-to-right; [labels] (e.g. W1-W4) render beneath, mono.
 */
@Composable
fun TrendChart(
    points: List<Float>,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 120.dp,
) {
    val line = MaterialTheme.semantic.accent
    val fill = Brush.verticalGradient(
        listOf(MaterialTheme.semantic.accent.copy(alpha = 0.35f), Color.Transparent),
    )
    Canvas(modifier = modifier.fillMaxWidth().height(chartHeight)) {
        if (points.size < 2) return@Canvas
        val maxV = (points.maxOrNull() ?: 1f).coerceAtLeast(0.0001f)
        val stepX = size.width / (points.size - 1)
        fun y(v: Float) = size.height - (v / maxV) * size.height * 0.9f - size.height * 0.05f
        val linePath = Path()
        val areaPath = Path()
        points.forEachIndexed { i, v ->
            val x = i * stepX
            val yy = y(v)
            if (i == 0) {
                linePath.moveTo(x, yy)
                areaPath.moveTo(x, size.height)
                areaPath.lineTo(x, yy)
            } else {
                linePath.lineTo(x, yy)
                areaPath.lineTo(x, yy)
            }
        }
        areaPath.lineTo(size.width, size.height)
        areaPath.close()
        drawPath(areaPath, brush = fill)
        drawPath(linePath, color = line, style = Stroke(width = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
    }
}


/**
 * Activity heatmap: [columns] x [rows] grid; each cell shaded by its 0..1 intensity, low values
 * nearly transparent and high values full accent.
 */
@Composable
fun Heatmap(
    intensities: List<Float>,
    modifier: Modifier = Modifier,
    columns: Int = 18,
    rows: Int = 7,
    gap: Dp = 4.dp,
) {
    val accent = MaterialTheme.semantic.accent
    val base = MaterialTheme.semantic.hairline
    Canvas(modifier = modifier.fillMaxWidth().height((rows * 14).dp)) {
        val g = gap.toPx()
        val cell = (size.width - g * (columns - 1)) / columns
        for (c in 0 until columns) {
            for (r in 0 until rows) {
                val idx = c * rows + r
                val v = intensities.getOrElse(idx) { 0f }.coerceIn(0f, 1f)
                val color = if (v <= 0f) base else accent.copy(alpha = 0.18f + 0.82f * v)
                drawRoundRect(
                    color = color,
                    topLeft = Offset(c * (cell + g), r * (cell + g)),
                    size = Size(cell, cell),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()),
                )
            }
        }
    }
}
