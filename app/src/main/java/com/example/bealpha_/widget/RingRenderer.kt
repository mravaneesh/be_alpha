package com.example.bealpha_.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader

/**
 * Renders the Apex progress ring (gradient arc + track, round caps, -90 deg start) to a Bitmap,
 * since Glance can't draw arcs. The centre label/check is overlaid with Glance composables.
 */
object RingRenderer {

    private const val SIZE_PX = 220

    fun ring(progress: Float, dark: Boolean, strokePx: Float = 24f): Bitmap {
        val bmp = Bitmap.createBitmap(SIZE_PX, SIZE_PX, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val pad = strokePx / 2f + 2f
        val rect = RectF(pad, pad, SIZE_PX - pad, SIZE_PX - pad)

        val track = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = strokePx
            color = if (dark) 0x14FFFFFF else 0x1A0F1E32
        }
        canvas.drawArc(rect, 0f, 360f, false, track)

        val sweep = progress.coerceIn(0f, 1f) * 360f
        if (sweep > 0f) {
            val arc = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = strokePx
                strokeCap = Paint.Cap.ROUND
                shader = LinearGradient(
                    0f, 0f, SIZE_PX.toFloat(), SIZE_PX.toFloat(),
                    intArrayOf(0xFF6FB0FF.toInt(), 0xFF4D9BFF.toInt(), 0xFF2F6FE0.toInt()),
                    floatArrayOf(0f, 0.55f, 1f),
                    Shader.TileMode.CLAMP,
                )
            }
            canvas.drawArc(rect, -90f, sweep, false, arc)
        }
        return bmp
    }

    /**
     * A vertical gradient (transparent at top → the widget background at the bottom) used as a
     * bottom overlay on the scrollable habit list, so the last row fades out and signals "scroll".
     */
    fun bottomFade(dark: Boolean): Bitmap {
        val w = 8
        val h = 64
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val bg = if (dark) 0xFF0D0E11.toInt() else 0xFFFFFFFF.toInt()
        val transparent = bg and 0x00FFFFFF
        val paint = Paint().apply {
            shader = LinearGradient(0f, 0f, 0f, h.toFloat(), transparent, bg, Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        return bmp
    }
}
