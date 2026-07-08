package com.derfence.astroface.wear.dial

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import com.derfence.astroface.wear.astro.AstroObserver
import com.derfence.astroface.wear.status.BatteryStatus
import com.derfence.astroface.wear.status.WatchStatus
import com.derfence.astroface.wear.status.WatchStatusSource
import java.time.Clock
import java.time.Instant
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos

class StatusOverlayRenderer(
    private val clock: Clock = Clock.system(AstroObserver.DEFAULT.zoneId),
    private val statusSource: WatchStatusSource
) : DialRenderer {
    override val contentDescription = "Date, batterie et phase de Lune AstroFace"
    override val validUntil: Instant?
        get() = lastRenderedStatus?.moonPhase?.validUntil

    private var lastRenderedStatus: WatchStatus? = null

    override fun render(): Bitmap {
        val bitmap = Bitmap.createBitmap(
            DialGeometry.canvasSize,
            DialGeometry.canvasSize,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val status = statusSource.statusAt(clock.instant())
        lastRenderedStatus = status

        drawMoonPhase(canvas, paint, status.moonPhase.phaseAngleDegrees)
        drawBattery(canvas, paint, status.battery)
        drawDate(canvas, paint, status.dateLabel)

        return bitmap
    }

    private fun drawMoonPhase(canvas: Canvas, paint: Paint, phaseAngleDegrees: Double) {
        val normalizedPhase = normalizePhaseAngle(phaseAngleDegrees)
        val waxing = normalizedPhase <= FULL_MOON_PHASE_DEGREES
        val terminatorWidth = (abs(cos(normalizedPhase * PI / 180.0)) * MOON_RADIUS * 2.0).toFloat()

        canvas.save()
        canvas.clipPath(moonClipPath())
        drawNewMoon(canvas, paint)
        drawLitMoonHalf(canvas, paint, litRight = waxing)
        drawTerminatorCorrection(canvas, paint, normalizedPhase, terminatorWidth, waxing)
        canvas.restore()

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f
        paint.color = Color.rgb(225, 228, 232)
        canvas.drawCircle(MOON_CENTER_X, MOON_CENTER_Y, MOON_RADIUS, paint)
    }

    private fun drawNewMoon(canvas: Canvas, paint: Paint) {
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(8, 8, 10)
        canvas.drawCircle(MOON_CENTER_X, MOON_CENTER_Y, MOON_RADIUS, paint)
    }

    private fun drawLitMoonHalf(canvas: Canvas, paint: Paint, litRight: Boolean) {
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(235, 238, 242)
        canvas.drawArc(
            moonBounds(),
            if (litRight) -90f else 90f,
            180f,
            true,
            paint
        )
    }

    private fun drawTerminatorCorrection(
        canvas: Canvas,
        paint: Paint,
        normalizedPhase: Double,
        terminatorWidth: Float,
        waxing: Boolean
    ) {
        if (terminatorWidth < MIN_TERMINATOR_WIDTH) {
            return
        }
        paint.style = Paint.Style.FILL
        paint.color = terminatorCorrectionColor(normalizedPhase)
        canvas.drawOval(
            RectF(
                MOON_CENTER_X - terminatorWidth / 2f,
                MOON_CENTER_Y - MOON_RADIUS,
                MOON_CENTER_X + terminatorWidth / 2f,
                MOON_CENTER_Y + MOON_RADIUS
            ),
            paint
        )
    }

    private fun terminatorCorrectionColor(normalizedPhase: Double): Int =
        when {
            normalizedPhase < FIRST_QUARTER_PHASE_DEGREES -> MOON_SHADOW_COLOR
            normalizedPhase < LAST_QUARTER_PHASE_DEGREES -> MOON_LIGHT_COLOR
            else -> MOON_SHADOW_COLOR
        }

    private fun normalizePhaseAngle(phaseAngleDegrees: Double): Double {
        val remainder = phaseAngleDegrees % FULL_CIRCLE_DEGREES
        return if (remainder < 0.0) remainder + FULL_CIRCLE_DEGREES else remainder
    }

    private fun drawBattery(canvas: Canvas, paint: Paint, battery: BatteryStatus) {
        val fillColor = batteryColor(battery.percent)
        val fillWidth = BATTERY_FILL_WIDTH * ((battery.percent ?: 0) / 100f)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.4f
        paint.color = STATUS_TEXT_COLOR
        canvas.drawRoundRect(BATTERY_BODY_BOUNDS, 2f, 2f, paint)

        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(BATTERY_TERMINAL_BOUNDS, 1f, 1f, paint)

        if (battery.percent != null && fillWidth > 0f) {
            paint.color = fillColor
            canvas.drawRoundRect(
                RectF(
                    BATTERY_BODY_BOUNDS.left + 2f,
                    BATTERY_BODY_BOUNDS.top + 2f,
                    BATTERY_BODY_BOUNDS.left + 2f + fillWidth,
                    BATTERY_BODY_BOUNDS.bottom - 2f
                ),
                1f,
                1f,
                paint
            )
        }
    }

    private fun batteryColor(percent: Int?): Int =
        when {
            percent == null -> STATUS_TEXT_COLOR
            percent > HIGH_BATTERY_THRESHOLD_PERCENT -> HIGH_BATTERY_COLOR
            percent <= LOW_BATTERY_THRESHOLD_PERCENT -> LOW_BATTERY_COLOR
            percent <= WARNING_BATTERY_THRESHOLD_PERCENT -> WARNING_BATTERY_COLOR
            else -> STATUS_TEXT_COLOR
        }

    private fun drawDate(canvas: Canvas, paint: Paint, dateLabel: String) {
        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.textSize = 16f
        paint.color = STATUS_TEXT_COLOR
        canvas.drawText(dateLabel, DialGeometry.center, DATE_BASELINE, paint)
    }

    private fun moonBounds(): RectF =
        RectF(
            MOON_CENTER_X - MOON_RADIUS,
            MOON_CENTER_Y - MOON_RADIUS,
            MOON_CENTER_X + MOON_RADIUS,
            MOON_CENTER_Y + MOON_RADIUS
        )

    private fun moonClipPath(): Path =
        Path().apply {
            addCircle(MOON_CENTER_X, MOON_CENTER_Y, MOON_RADIUS, Path.Direction.CW)
        }

    private companion object {
        private const val FULL_CIRCLE_DEGREES = 360.0
        private const val FIRST_QUARTER_PHASE_DEGREES = 90.0
        private const val FULL_MOON_PHASE_DEGREES = 180.0
        private const val LAST_QUARTER_PHASE_DEGREES = 270.0
        private const val TOP_STATUS_Y = 52f
        private const val SIDE_STATUS_OFFSET_X = 42f
        private const val MOON_CENTER_X = DialGeometry.center - SIDE_STATUS_OFFSET_X
        private const val MOON_CENTER_Y = TOP_STATUS_Y
        private const val MOON_RADIUS = 10f
        private const val MIN_TERMINATOR_WIDTH = 0.05f
        private const val DATE_BASELINE = DialGeometry.center + 60f
        private val BATTERY_BODY_BOUNDS = RectF(
            DialGeometry.center + SIDE_STATUS_OFFSET_X - 12f,
            TOP_STATUS_Y - 6f,
            DialGeometry.center + SIDE_STATUS_OFFSET_X + 12f,
            TOP_STATUS_Y + 6f
        )
        private val BATTERY_TERMINAL_BOUNDS = RectF(
            BATTERY_BODY_BOUNDS.right + 1f,
            TOP_STATUS_Y - 2f,
            BATTERY_BODY_BOUNDS.right + 4f,
            TOP_STATUS_Y + 2f
        )
        private const val BATTERY_FILL_WIDTH = 20f
        private const val HIGH_BATTERY_THRESHOLD_PERCENT = 80
        private const val WARNING_BATTERY_THRESHOLD_PERCENT = 30
        private const val LOW_BATTERY_THRESHOLD_PERCENT = 20
        private val STATUS_TEXT_COLOR = Color.rgb(245, 245, 242)
        private val HIGH_BATTERY_COLOR = Color.rgb(64, 210, 112)
        private val WARNING_BATTERY_COLOR = Color.rgb(255, 149, 42)
        private val LOW_BATTERY_COLOR = Color.rgb(229, 57, 53)
        private val MOON_LIGHT_COLOR = Color.rgb(235, 238, 242)
        private val MOON_SHADOW_COLOR = Color.rgb(8, 8, 10)
    }
}
