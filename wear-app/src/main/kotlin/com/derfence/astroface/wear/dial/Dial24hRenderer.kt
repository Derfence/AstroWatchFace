package com.derfence.astroface.wear.dial

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface

class Dial24hRenderer : DialRenderer {
    override val contentDescription = "Cadran AstroFace 24 heures"

    override fun render(): Bitmap {
        val bitmap = Bitmap.createBitmap(
            DialGeometry.canvasSize,
            DialGeometry.canvasSize,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.style = Paint.Style.STROKE
        paint.color = Color.argb(210, 120, 12, 20)
        paint.strokeWidth = 2.5f
        canvas.drawCircle(DialGeometry.center, DialGeometry.center, 216f, paint)

        drawTicks(canvas, paint)
        drawLabels(canvas, paint)

        return bitmap
    }

    private fun drawTicks(canvas: Canvas, paint: Paint) {
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.BUTT

        DialGeometry.twentyFourHourTicks().forEach { tick ->
            val outer = DialGeometry.point(216f, tick.angleDegrees)
            val inner = DialGeometry.point(if (tick.label != null) 196f else 205f, tick.angleDegrees)
            paint.color = if (tick.label != null) Color.WHITE else Color.argb(165, 255, 255, 255)
            paint.strokeWidth = if (tick.label != null) 2.4f else 1.3f
            canvas.drawLine(inner.x, inner.y, outer.x, outer.y, paint)
        }
    }

    private fun drawLabels(canvas: Canvas, paint: Paint) {
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.textSize = 18f

        DialGeometry.twentyFourHourTicks()
            .filter { it.label != null }
            .forEach { tick ->
                val point = DialGeometry.point(178f, tick.angleDegrees)
                canvas.drawText(tick.label.orEmpty(), point.x, point.y + 6f, paint)
            }
    }
}
