package com.derfence.astroface.wear.dial

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.derfence.astroface.wear.astro.AstroObserver
import com.derfence.astroface.wear.status.BatteryStatus
import com.derfence.astroface.wear.status.WatchStatus
import com.derfence.astroface.wear.status.WatchStatusSource
import java.time.Clock
import java.time.Instant

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
        MoonPhasePainter.draw(
            canvas = canvas,
            paint = paint,
            centerX = MOON_CENTER_X,
            centerY = MOON_CENTER_Y,
            radius = MOON_RADIUS,
            phaseAngleDegrees = phaseAngleDegrees
        )
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
        paint.textSize = DATE_TEXT_SIZE
        paint.color = STATUS_TEXT_COLOR
        canvas.drawText(dateLabel, DialGeometry.center, DATE_BASELINE, paint)
    }

    private companion object {
        private const val DATE_VERTICAL_OFFSET = 70f
        private const val MOON_CENTER_X = DialGeometry.center
        private const val MOON_CENTER_Y = DialGeometry.center - DATE_VERTICAL_OFFSET
        private const val MOON_RADIUS = 25f
        private const val DATE_BASELINE = DialGeometry.center + DATE_VERTICAL_OFFSET
        private const val DATE_TEXT_SIZE = 18f
        private const val BATTERY_CENTER_Y = DialGeometry.center - 182f
        private val BATTERY_BODY_BOUNDS = RectF(
            DialGeometry.center - 12f,
            BATTERY_CENTER_Y - 6f,
            DialGeometry.center + 12f,
            BATTERY_CENTER_Y + 6f
        )
        private val BATTERY_TERMINAL_BOUNDS = RectF(
            BATTERY_BODY_BOUNDS.right + 1f,
            BATTERY_CENTER_Y - 2f,
            BATTERY_BODY_BOUNDS.right + 4f,
            BATTERY_CENTER_Y + 2f
        )
        private const val BATTERY_FILL_WIDTH = 20f
        private const val HIGH_BATTERY_THRESHOLD_PERCENT = 80
        private const val WARNING_BATTERY_THRESHOLD_PERCENT = 30
        private const val LOW_BATTERY_THRESHOLD_PERCENT = 20
        private val STATUS_TEXT_COLOR = Color.rgb(245, 245, 242)
        private val HIGH_BATTERY_COLOR = Color.rgb(64, 210, 112)
        private val WARNING_BATTERY_COLOR = Color.rgb(255, 149, 42)
        private val LOW_BATTERY_COLOR = Color.rgb(229, 57, 53)
    }
}
