package com.derfence.astroface.wear.dial

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.derfence.astroface.wear.astro.ConstellationLine
import com.derfence.astroface.wear.astro.SkyPoint

class ConstellationBackgroundRenderer(
    private val screenRadius: Float = DialGeometry.center,
    private val skyRadiusDegrees: Double = 100.0,
    private val lineColor: Int = Color.argb(122, 210, 38, 46),
    private val starColor: Int = Color.argb(88, 245, 226, 226)
) {
    fun render(
        canvas: Canvas,
        paint: Paint,
        lines: List<ConstellationLine>
    ) {
        if (lines.isEmpty()) {
            return
        }

        canvas.save()
        canvas.clipPath(Path().apply {
            addCircle(
                DialGeometry.center,
                DialGeometry.center,
                screenRadius,
                Path.Direction.CW
            )
        })

        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 1f
        paint.color = lineColor

        lines.forEach { line ->
            val from = pointForSkyPoint(line.from)
            val to = pointForSkyPoint(line.to)
            canvas.drawLine(from.x, from.y, to.x, to.y, paint)
        }

        paint.style = Paint.Style.FILL
        paint.color = starColor
        lines.forEach { line ->
            drawStar(canvas, paint, line.from)
            drawStar(canvas, paint, line.to)
        }

        canvas.restore()
    }

    private fun drawStar(canvas: Canvas, paint: Paint, point: SkyPoint) {
        if (point.zenithDistanceDegrees > skyRadiusDegrees) {
            return
        }

        val screenPoint = pointForSkyPoint(point)
        canvas.drawCircle(screenPoint.x, screenPoint.y, 1.2f, paint)
    }

    private fun pointForSkyPoint(point: SkyPoint): DialPoint {
        val radius = (point.zenithDistanceDegrees.coerceAtLeast(0.0) *
            screenRadius.toDouble() / skyRadiusDegrees).toFloat()
        return DialGeometry.point(radius, angleForSkyAzimuth(point.azimuthDegrees))
    }

    private fun angleForSkyAzimuth(azimuthDegrees: Double): Float {
        val remainder = -azimuthDegrees % FULL_CIRCLE_DEGREES
        return (if (remainder < 0.0) remainder + FULL_CIRCLE_DEGREES else remainder).toFloat()
    }

    private companion object {
        private const val FULL_CIRCLE_DEGREES = 360.0
    }
}
