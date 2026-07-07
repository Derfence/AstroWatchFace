package com.derfence.astroface.wear.dial

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class Dial12hRenderer : DialRenderer {
    override val contentDescription = "Repères analogiques AstroFace"

    override fun render(): Bitmap {
        val bitmap = Bitmap.createBitmap(
            DialGeometry.canvasSize,
            DialGeometry.canvasSize,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.style = Paint.Style.STROKE
        paint.color = Color.argb(190, 255, 255, 255)
        paint.strokeWidth = 2f
        canvas.drawCircle(DialGeometry.center, DialGeometry.center, 150f, paint)

        DialGeometry.twelveHourTicks().forEach { tick ->
            val outer = DialGeometry.point(150f, tick.angleDegrees)
            val inner = DialGeometry.point(if (tick.isMajor) 129f else 137f, tick.angleDegrees)
            paint.color = if (tick.isMajor) Color.argb(230, 229, 57, 53) else Color.WHITE
            paint.strokeWidth = if (tick.isMajor) 3.2f else 2f
            canvas.drawLine(inner.x, inner.y, outer.x, outer.y, paint)
        }

        return bitmap
    }
}
