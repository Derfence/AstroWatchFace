package com.derfence.astroface.wear.dial

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.SweepGradient
import com.derfence.astroface.wear.astro.CelestialBody
import com.derfence.astroface.wear.astro.SolarSystemBody

object OrbitTailPainter {
    fun draw(
        canvas: Canvas,
        paint: Paint,
        radius: Float,
        angleDegrees: Float,
        baseColor: Int
    ) {
        val bounds = RectF(
            DialGeometry.center - radius,
            DialGeometry.center - radius,
            DialGeometry.center + radius,
            DialGeometry.center + radius
        )
        val tailStart = angleDegrees - CANVAS_ARC_OFFSET_DEGREES - TAIL_SWEEP_DEGREES

        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = TAIL_STROKE_WIDTH
        paint.shader = tailGradient(baseColor, tailStart)
        canvas.drawArc(
            bounds,
            tailStart,
            TAIL_SWEEP_DEGREES,
            false,
            paint
        )
        paint.shader = null
    }

    fun colorFor(body: CelestialBody): Int =
        when (body) {
            CelestialBody.SUN -> Color.argb(200, 255, 204, 82)
            CelestialBody.MOON -> Color.argb(200, 235, 238, 242)
            CelestialBody.MERCURY -> Color.argb(200, 166, 145, 119)
            CelestialBody.VENUS -> Color.argb(200, 255, 188, 48)
            CelestialBody.MARS -> Color.argb(200, 214, 68, 48)
            CelestialBody.JUPITER -> Color.argb(200, 229, 169, 98)
            CelestialBody.SATURN -> Color.argb(200, 238, 215, 142)
            CelestialBody.URANUS -> Color.argb(200, 104, 211, 210)
            CelestialBody.NEPTUNE -> Color.argb(200, 69, 113, 225)
        }

    fun colorFor(body: SolarSystemBody): Int =
        when (body) {
            SolarSystemBody.SUN -> Color.argb(255, 255, 204, 82)
            SolarSystemBody.MERCURY -> Color.argb(255, 166, 145, 119)
            SolarSystemBody.VENUS -> Color.argb(255, 255, 188, 48)
            SolarSystemBody.EARTH -> Color.argb(255, 58, 139, 220)
            SolarSystemBody.MARS -> Color.argb(255, 214, 68, 48)
            SolarSystemBody.JUPITER -> Color.argb(255, 229, 169, 98)
            SolarSystemBody.SATURN -> Color.argb(255, 238, 215, 142)
            SolarSystemBody.URANUS -> Color.argb(255, 104, 211, 210)
            SolarSystemBody.NEPTUNE -> Color.argb(255, 69, 113, 225)
        }

    private fun tailGradient(baseColor: Int, tailStartDegrees: Float): SweepGradient {
        val gradient = SweepGradient(
            DialGeometry.center,
            DialGeometry.center,
            intArrayOf(
                baseColor.withAlpha(0),
                baseColor,
                baseColor.withAlpha(0),
                baseColor.withAlpha(0)
            ),
            floatArrayOf(
                0f,
                TAIL_SWEEP_DEGREES / FULL_CIRCLE_DEGREES,
                TAIL_SWEEP_DEGREES / FULL_CIRCLE_DEGREES + TAIL_GRADIENT_CLOSE_FRACTION,
                1f
            )
        )
        gradient.setLocalMatrix(Matrix().apply {
            setRotate(tailStartDegrees, DialGeometry.center, DialGeometry.center)
        })
        return gradient
    }

    private fun Int.withAlpha(alpha: Int): Int =
        Color.argb(alpha, Color.red(this), Color.green(this), Color.blue(this))

    private const val FULL_CIRCLE_DEGREES = 360f
    private const val CANVAS_ARC_OFFSET_DEGREES = 90f
    private const val TAIL_SWEEP_DEGREES = 100f
    private const val TAIL_STROKE_WIDTH = 2f
    private const val TAIL_GRADIENT_CLOSE_FRACTION = 0.001f
}
