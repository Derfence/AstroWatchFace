package com.derfence.astroface.wear.dial

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos

internal object MoonPhasePainter {
    fun draw(
        canvas: Canvas,
        paint: Paint,
        surface: Bitmap,
        centerX: Float,
        centerY: Float,
        radius: Float,
        phaseAngleDegrees: Double
    ) {
        val moonPath = moonPath(centerX, centerY, radius)

        canvas.save()
        canvas.clipPath(moonPath)
        drawSurface(canvas, paint, surface, centerX, centerY, radius)
        canvas.restore()

        if (SHOW_PHASE_SHADOW) {
            val bounds = RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
            val litPath = litPath(bounds, centerX, centerY, radius, normalizePhaseAngle(phaseAngleDegrees))
            val shadowPath = Path(moonPath).apply {
                op(litPath, Path.Op.DIFFERENCE)
            }

            paint.shader = null
            paint.style = Paint.Style.FILL
            paint.color = MOON_SHADOW_OVERLAY
            canvas.drawPath(shadowPath, paint)
        }

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = MOON_RIM_STROKE_WIDTH
        paint.color = MOON_RIM_COLOR
        canvas.drawCircle(centerX, centerY, radius, paint)
    }

    private fun drawSurface(
        canvas: Canvas,
        paint: Paint,
        surface: Bitmap,
        centerX: Float,
        centerY: Float,
        radius: Float
    ) {
        paint.shader = null
        paint.style = Paint.Style.FILL
        paint.isFilterBitmap = true
        canvas.drawBitmap(
            surface,
            null,
            RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius),
            paint
        )
    }

    private fun litPath(
        bounds: RectF,
        centerX: Float,
        centerY: Float,
        radius: Float,
        normalizedPhase: Double
    ): Path {
        val waxing = normalizedPhase <= FULL_MOON_PHASE_DEGREES
        val path = halfPath(bounds, centerX, centerY, litRight = waxing)
        val terminatorWidth = (abs(cos(normalizedPhase * PI / 180.0)) * radius * 2.0).toFloat()

        if (terminatorWidth < MIN_TERMINATOR_WIDTH) {
            return path
        }

        val terminator = Path().apply {
            addOval(
                RectF(
                    centerX - terminatorWidth / 2f,
                    centerY - radius,
                    centerX + terminatorWidth / 2f,
                    centerY + radius
                ),
                Path.Direction.CW
            )
        }

        if (normalizedPhase < FIRST_QUARTER_PHASE_DEGREES ||
            normalizedPhase >= LAST_QUARTER_PHASE_DEGREES
        ) {
            path.op(terminator, Path.Op.DIFFERENCE)
        } else {
            path.op(terminator, Path.Op.UNION)
            path.op(moonPath(centerX, centerY, radius), Path.Op.INTERSECT)
        }

        return path
    }

    private fun halfPath(
        bounds: RectF,
        centerX: Float,
        centerY: Float,
        litRight: Boolean
    ): Path =
        Path().apply {
            moveTo(centerX, bounds.top)
            arcTo(bounds, if (litRight) -90f else 270f, if (litRight) 180f else -180f, false)
            lineTo(centerX, bounds.top)
            close()
        }

    private fun moonPath(centerX: Float, centerY: Float, radius: Float): Path =
        Path().apply {
            addCircle(centerX, centerY, radius, Path.Direction.CW)
        }

    private fun normalizePhaseAngle(phaseAngleDegrees: Double): Double {
        val remainder = phaseAngleDegrees % FULL_CIRCLE_DEGREES
        return if (remainder < 0.0) remainder + FULL_CIRCLE_DEGREES else remainder
    }

    private const val FULL_CIRCLE_DEGREES = 360.0
    private const val FIRST_QUARTER_PHASE_DEGREES = 90.0
    private const val FULL_MOON_PHASE_DEGREES = 180.0
    private const val LAST_QUARTER_PHASE_DEGREES = 270.0
    private const val MIN_TERMINATOR_WIDTH = 0.05f
    private const val MOON_RIM_STROKE_WIDTH = 0.7f
    private const val SHOW_PHASE_SHADOW = true
    private val MOON_SHADOW_OVERLAY = Color.argb(238, 5, 6, 9)
    private val MOON_RIM_COLOR = Color.rgb(225, 228, 232)
}
