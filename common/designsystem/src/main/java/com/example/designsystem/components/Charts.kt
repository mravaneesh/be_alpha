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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.theme.PactType
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
 * Trend chart: a marker (dot) at each data point connected by an accent line, over a gradient
 * area fill. [points] are 0..1 values left-to-right (e.g. weekly completion fractions).
 *
 * The y-axis is a **fixed 0..1 scale** — a point at 0.6 always sits at 60% height, never rescaled
 * to the series peak — so heights reflect the true completion fraction and can be compared across
 * charts and over time. 0/50/100% gridlines label the y-axis; [xLabels] (one per point, e.g.
 * "5w".."Now") label the x-axis in mono beneath the plot. [xAxisTitle]/[yAxisTitle] optionally
 * caption what unit those tick labels are in (e.g. "Weeks" / "%").
 */
@Composable
fun TrendChart(
    points: List<Float>,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 120.dp,
    xLabels: List<String> = emptyList(),
    xAxisTitle: String = "",
    yAxisTitle: String = "",
) {
    val line = MaterialTheme.semantic.accent
    val dotCenter = MaterialTheme.colorScheme.surface
    val grid = MaterialTheme.semantic.hairline
    val labelStyle = PactType.mono.copy(fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    val measurer = rememberTextMeasurer()
    val fill = Brush.verticalGradient(
        listOf(MaterialTheme.semantic.accent.copy(alpha = 0.35f), Color.Transparent),
    )
    Canvas(modifier = modifier.fillMaxWidth().height(chartHeight + if (xAxisTitle.isEmpty()) 0.dp else 14.dp)) {
        if (points.isEmpty()) return@Canvas
        // Gutters reserve room for the axis labels (and titles, if given); the plot fills the rest.
        val yTitleW = if (yAxisTitle.isEmpty()) 0f else 14.dp.toPx()
        val xLabelRowH = if (xLabels.isEmpty()) 0f else 16.dp.toPx()
        val xTitleRowH = if (xAxisTitle.isEmpty()) 0f else 14.dp.toPx()
        val gutterL = 32.dp.toPx() + yTitleW
        val gutterB = xLabelRowH + xTitleRowH
        val plotW = size.width - gutterL
        val plotH = size.height - gutterB
        // Inset top & bottom so a full (1.0) or empty (0.0) point isn't clipped at the edge.
        val padTop = plotH * 0.08f
        val usable = plotH * 0.84f
        fun y(v: Float) = padTop + (1f - v.coerceIn(0f, 1f)) * usable
        // Inset left & right so the first/last points sit clear of the y-axis and the edge.
        val padX = 10.dp.toPx()
        val usableW = plotW - 2f * padX
        fun x(i: Int) = gutterL + padX + if (points.size == 1) usableW / 2f else i * (usableW / (points.size - 1))

        // Y axis: gridlines + labels at 0 / 50 / 100%.
        listOf(0f, 0.5f, 1f).forEach { v ->
            val yy = y(v)
            drawLine(grid, Offset(gutterL, yy), Offset(size.width, yy), strokeWidth = 1.dp.toPx())
            val t = measurer.measure(AnnotatedString("${(v * 100).toInt()}%"), labelStyle)
            drawText(t, topLeft = Offset(gutterL - t.size.width - 6.dp.toPx(), yy - t.size.height / 2f))
        }

        if (points.size >= 2) {
            val base = y(0f)
            val linePath = Path()
            val areaPath = Path()
            points.forEachIndexed { i, v ->
                val xx = x(i)
                val yy = y(v)
                if (i == 0) {
                    linePath.moveTo(xx, yy)
                    areaPath.moveTo(xx, base)
                    areaPath.lineTo(xx, yy)
                } else {
                    linePath.lineTo(xx, yy)
                    areaPath.lineTo(xx, yy)
                }
            }
            areaPath.lineTo(x(points.lastIndex), base)
            areaPath.close()
            drawPath(areaPath, brush = fill)
            drawPath(linePath, color = line, style = Stroke(width = 2.5.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
        }

        // Markers: a filled center (surface) ringed in accent so each data point reads clearly.
        val outer = 5.dp.toPx()
        points.forEachIndexed { i, v ->
            val c = Offset(x(i), y(v))
            drawCircle(color = dotCenter, radius = outer, center = c)
            drawCircle(color = line, radius = outer, center = c, style = Stroke(width = 2.dp.toPx()))
        }

        // X axis: one label per point, centered under its marker (clamped inside the plot).
        xLabels.forEachIndexed { i, label ->
            if (i >= points.size) return@forEachIndexed
            val t = measurer.measure(AnnotatedString(label), labelStyle)
            val cx = (x(i) - t.size.width / 2f).coerceIn(gutterL, size.width - t.size.width)
            drawText(t, topLeft = Offset(cx, plotH + 3.dp.toPx()))
        }

        // Axis titles: what unit the tick labels above/left are in.
        if (xAxisTitle.isNotEmpty()) {
            val t = measurer.measure(AnnotatedString(xAxisTitle), labelStyle)
            val cx = gutterL + (plotW - t.size.width) / 2f
            drawText(t, topLeft = Offset(cx, plotH + xLabelRowH))
        }
        if (yAxisTitle.isNotEmpty()) {
            val t = measurer.measure(AnnotatedString(yAxisTitle), labelStyle)
            rotate(degrees = -90f, pivot = Offset(yTitleW / 2f, plotH / 2f)) {
                drawText(t, topLeft = Offset(yTitleW / 2f - t.size.width / 2f, plotH / 2f - t.size.height / 2f))
            }
        }
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
