package com.derfence.astroface.wear.dial

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt

internal object MoonPhasePainter {
    fun draw(
        canvas: Canvas,
        paint: Paint,
        centerX: Float,
        centerY: Float,
        radius: Float,
        phaseAngleDegrees: Double
    ) {
        val moonPath = moonPath(centerX, centerY, radius)

        canvas.save()
        canvas.clipPath(moonPath)
        drawSurface(canvas, paint, centerX, centerY, radius)
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
        paint.strokeWidth = radius * SVG_RIM_STROKE_WIDTH / SVG_RADIUS
        paint.color = MOON_RIM_COLOR
        canvas.drawCircle(centerX, centerY, radius, paint)
    }

    private fun drawSurface(
        canvas: Canvas,
        paint: Paint,
        centerX: Float,
        centerY: Float,
        radius: Float
    ) {
        paint.style = Paint.Style.FILL
        paint.shader = RadialGradient(
            svgToLocal(centerX = centerX, radius = radius, svgCoordinate = 97.76f),
            svgToLocal(centerX = centerY, radius = radius, svgCoordinate = 86.96f),
            radius * SVG_BASE_GRADIENT_RADIUS / SVG_RADIUS,
            intArrayOf(
                Color.rgb(248, 248, 241),
                Color.rgb(215, 216, 211),
                Color.rgb(139, 143, 144)
            ),
            floatArrayOf(0f, 0.64f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(centerX, centerY, radius, paint)
        paint.shader = null

        canvas.save()
        canvas.translate(centerX, centerY)
        val scale = radius / SVG_RADIUS
        canvas.scale(scale, scale)
        canvas.translate(-SVG_CENTER, -SVG_CENTER)
        drawSvgMaria(canvas, paint)
        drawSvgCraters(canvas, paint)
        canvas.restore()
    }

    private fun drawSvgMaria(canvas: Canvas, paint: Paint) {
        paint.style = Paint.Style.FILL
        paint.color = Color.argb((MARIA_OPACITY * MAX_ALPHA).roundToInt(), 70, 76, 80)

        SVG_MARIA.forEach { oval ->
            canvas.save()
            canvas.concat(oval.matrix.toAndroidMatrix())
            canvas.drawOval(oval.bounds, paint)
            canvas.restore()
        }
    }

    private fun drawSvgCraters(canvas: Canvas, paint: Paint) {
        SVG_CRATERS.forEach { circle ->
            paint.style = circle.style
            paint.color = circle.color
            paint.strokeWidth = circle.strokeWidth

            canvas.save()
            canvas.concat(circle.matrix.toAndroidMatrix())
            canvas.drawCircle(circle.centerX, circle.centerY, circle.radius, paint)
            canvas.restore()
        }
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

    private data class SvgMatrix(
        val scaleX: Float,
        val skewY: Float,
        val skewX: Float,
        val scaleY: Float,
        val translateX: Float,
        val translateY: Float
    ) {
        fun toAndroidMatrix(): Matrix =
            Matrix().apply {
                setValues(
                    floatArrayOf(
                        scaleX,
                        skewX,
                        translateX,
                        skewY,
                        scaleY,
                        translateY,
                        0f,
                        0f,
                        1f
                    )
                )
            }
    }

    private data class SvgOval(
        val centerX: Float,
        val centerY: Float,
        val radiusX: Float,
        val radiusY: Float,
        val matrix: SvgMatrix
    ) {
        val bounds: RectF =
            RectF(centerX - radiusX, centerY - radiusY, centerX + radiusX, centerY + radiusY)
    }

    private data class SvgCircle(
        val centerX: Float,
        val centerY: Float,
        val radius: Float,
        val style: Paint.Style,
        val color: Int,
        val strokeWidth: Float,
        val matrix: SvgMatrix
    )

    private fun svgToLocal(centerX: Float, radius: Float, svgCoordinate: Float): Float =
        centerX + (svgCoordinate - SVG_CENTER) * radius / SVG_RADIUS

    private fun fillColor(alpha: Float): Int =
        Color.argb((alpha * MAX_ALPHA).roundToInt(), 50, 54, 58)

    private fun strokeColor(alpha: Float): Int =
        Color.argb((alpha * MAX_ALPHA).roundToInt(), 238, 238, 229)

    private const val FULL_CIRCLE_DEGREES = 360.0
    private const val FIRST_QUARTER_PHASE_DEGREES = 90.0
    private const val FULL_MOON_PHASE_DEGREES = 180.0
    private const val LAST_QUARTER_PHASE_DEGREES = 270.0
    private const val MIN_TERMINATOR_WIDTH = 0.05f
    private const val MAX_ALPHA = 255
    private const val SVG_CENTER = 128f
    private const val SVG_RADIUS = 108f
    private const val SVG_BASE_GRADIENT_RADIUS = 155.52f
    private const val SVG_RIM_STROKE_WIDTH = 3f
    private const val MARIA_OPACITY = 0.39f
    private const val SHOW_PHASE_SHADOW = true
    private val MOON_SHADOW_OVERLAY = Color.argb(238, 5, 6, 9)
    private val MOON_RIM_COLOR = Color.rgb(225, 228, 232)

    private val SVG_MARIA = listOf(
        SvgOval(
            centerX = 87f,
            centerY = 104.2f,
            radiusX = 21.6f,
            radiusY = 15.1f,
            matrix = SvgMatrix(1.331561f, -0.43265f, 0.50662f, 1.559217f, -64.270547f, -44.222354f)
        ),
        SvgOval(
            centerX = 103.2f,
            centerY = 117.2f,
            radiusX = 17.3f,
            radiusY = 10.8f,
            matrix = SvgMatrix(-0.160165f, 2.041402f, -2.817301f, -0.22104f, 438.939973f, -62.403195f)
        ),
        SvgOval(
            centerX = 136.6f,
            centerY = 94.5f,
            radiusX = 18.4f,
            radiusY = 11.9f,
            matrix = SvgMatrix(0.954456f, 0.492928f, -0.566627f, 1.09716f, 83.373368f, -80.767044f)
        ),
        SvgOval(
            centerX = 153.9f,
            centerY = 106.4f,
            radiusX = 16.2f,
            radiusY = 9.7f,
            matrix = SvgMatrix(0.310601f, 0.732697f, -0.787594f, 0.333873f, 252.709741f, -48.592664f)
        ),
        SvgOval(
            centerX = 164.7f,
            centerY = 135.6f,
            radiusX = 18.4f,
            radiusY = 13f,
            matrix = SvgMatrix(0.187751f, 0.875373f, -0.94743f, 0.203206f, 308.008728f, -35.039842f)
        ),
        SvgOval(
            centerX = 147.4f,
            centerY = 147.4f,
            radiusX = 13f,
            radiusY = 8.6f,
            matrix = SvgMatrix(0.68215f, 1.090367f, -2.228565f, 1.394224f, 409.592727f, -247.579114f)
        ),
        SvgOval(
            centerX = 112.9f,
            centerY = 153.9f,
            radiusX = 28.1f,
            radiusY = 14f,
            matrix = SvgMatrix(-0.043686f, 2.008766f, -1.787818f, -0.038881f, 329.580977f, -121.201271f)
        ),
        SvgOval(
            centerX = 90.2f,
            centerY = 141f,
            radiusX = 11.9f,
            radiusY = 8.6f,
            matrix = SvgMatrix(1.364737f, -0.820017f, 0.688204f, 1.145363f, -118.708352f, 90.474908f)
        ),
        SvgOval(
            centerX = 90.2f,
            centerY = 141f,
            radiusX = 11.9f,
            radiusY = 8.6f,
            matrix = SvgMatrix(0.691534f, 0.722344f, -0.722344f, 0.691534f, 108.327244f, 19.535481f)
        ),
        SvgOval(
            centerX = 90.2f,
            centerY = 141f,
            radiusX = 11.9f,
            radiusY = 8.6f,
            matrix = SvgMatrix(0.691534f, 0.722344f, -0.722344f, 0.691534f, 225.451771f, -4.895404f)
        ),
        SvgOval(
            centerX = 90.2f,
            centerY = 141f,
            radiusX = 11.9f,
            radiusY = 8.6f,
            matrix = SvgMatrix(0.832114f, 0.052764f, -0.05564f, 0.877477f, 21.761173f, 34.552134f)
        ),
        SvgOval(
            centerX = 90.2f,
            centerY = 141f,
            radiusX = 11.9f,
            radiusY = 8.6f,
            matrix = SvgMatrix(1.344502f, -0.47192f, 0.450277f, 1.282842f, -48.605395f, -24.37918f)
        ),
        SvgOval(
            centerX = 90.2f,
            centerY = 141f,
            radiusX = 11.9f,
            radiusY = 8.6f,
            matrix = SvgMatrix(0.512316f, -0.345372f, 0.344841f, 0.511529f, -7.337873f, 20.442804f)
        ),
        SvgOval(
            centerX = 90.2f,
            centerY = 141f,
            radiusX = 11.9f,
            radiusY = 8.6f,
            matrix = SvgMatrix(0.877387f, 0.898297f, -0.939996f, 0.918116f, 120.575615f, -52.474946f)
        )
    )

    private val SVG_CRATERS = listOf(
        SvgCircle(74f, 160.4f, 7.1f, Paint.Style.FILL, fillColor(0.28f), 0f, SvgMatrix(1f, 0f, 0f, 1f, 17.724371f, -38.322953f)),
        SvgCircle(72.7f, 159f, 7.1f, Paint.Style.STROKE, strokeColor(0.58f), 0.75f, SvgMatrix(1f, 0f, 0f, 1f, 17.724371f, -38.322953f)),
        SvgCircle(108.6f, 78.3f, 5.2f, Paint.Style.FILL, fillColor(0.23f), 0f, SvgMatrix(1f, 0f, 0f, 1f, 19.880038f, 47.9037f)),
        SvgCircle(107.6f, 77.3f, 5.2f, Paint.Style.STROKE, strokeColor(0.51f), 0.7f, SvgMatrix(1f, 0f, 0f, 1f, 19.880038f, 47.9037f)),
        SvgCircle(144.2f, 170.1f, 6f, Paint.Style.FILL, fillColor(0.26f), 0f, SvgMatrix(1f, 0f, 0f, 1f, -28.023673f, 42.155256f)),
        SvgCircle(143.1f, 168.9f, 6f, Paint.Style.STROKE, strokeColor(0.56f), 0.72f, SvgMatrix(1f, 0f, 0f, 1f, -28.023673f, 42.155256f)),
        SvgCircle(177.7f, 96.7f, 4.9f, Paint.Style.FILL, fillColor(0.22f), 0f, SvgMatrix(1f, 0f, 0f, 1f, -62.753849f, -45.987546f)),
        SvgCircle(176.8f, 95.7f, 4.9f, Paint.Style.STROKE, strokeColor(0.48f), 0.68f, SvgMatrix(1f, 0f, 0f, 1f, -62.753849f, -45.987546f)),
        SvgCircle(128f, 123.7f, 3.7f, Paint.Style.FILL, fillColor(0.17f), 0f, SvgMatrix(1f, 0f, 0f, 1f, 17.963875f, -11.975922f)),
        SvgCircle(127.3f, 122.9f, 3.7f, Paint.Style.STROKE, strokeColor(0.42f), 0.58f, SvgMatrix(1f, 0f, 0f, 1f, 17.963875f, -11.975922f)),
        SvgCircle(169f, 159.3f, 3.5f, Paint.Style.FILL, fillColor(0.17f), 0f, SvgMatrix(1f, 0f, 0f, 1f, 9.820272f, -115.926948f)),
        SvgCircle(168.4f, 158.6f, 3.5f, Paint.Style.STROKE, strokeColor(0.42f), 0.52f, SvgMatrix(1f, 0f, 0f, 1f, 9.820272f, -115.926948f)),
        SvgCircle(90.2f, 82.6f, 3.2f, Paint.Style.FILL, fillColor(0.15f), 0f, SvgMatrix(1f, 0f, 0f, 1f, -27.784136f, 42.873806f)),
        SvgCircle(90.2f, 82.6f, 3.2f, Paint.Style.FILL, fillColor(0.15f), 0f, SvgMatrix(1f, 0f, 0f, 1f, 114.968882f, 21.556658f)),
        SvgCircle(90.2f, 82.6f, 3.2f, Paint.Style.FILL, fillColor(0.15f), 0f, SvgMatrix(1f, 0f, 0f, 1f, 115.926941f, 107.064761f)),
        SvgCircle(90.2f, 82.6f, 3.2f, Paint.Style.FILL, fillColor(0.15f), 0f, SvgMatrix(1f, 0f, 0f, 1f, -37.604398f, 12.215437f)),
        SvgCircle(89.6f, 82f, 3.2f, Paint.Style.STROKE, strokeColor(0.38f), 0.5f, SvgMatrix(1f, 0f, 0f, 1f, -27.784136f, 42.873806f)),
        SvgCircle(89.6f, 82f, 3.2f, Paint.Style.STROKE, strokeColor(0.38f), 0.5f, SvgMatrix(1f, 0f, 0f, 1f, 114.968882f, 21.556658f)),
        SvgCircle(89.6f, 82f, 3.2f, Paint.Style.STROKE, strokeColor(0.38f), 0.5f, SvgMatrix(1f, 0f, 0f, 1f, 115.926941f, 107.064761f))
    )
}
