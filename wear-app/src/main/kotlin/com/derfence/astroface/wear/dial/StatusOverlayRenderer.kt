package com.derfence.astroface.wear.dial

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.derfence.astroface.wear.astro.AstroObserver
import com.derfence.astroface.wear.astro.MoonPhaseKind
import com.derfence.astroface.wear.status.BatteryStatus
import com.derfence.astroface.wear.status.WatchStatusSource
import java.time.Clock

class StatusOverlayRenderer(
    private val clock: Clock = Clock.system(AstroObserver.DEFAULT.zoneId),
    private val statusSource: WatchStatusSource
) : DialRenderer {
    override val contentDescription = "Date, batterie et phase de Lune AstroFace"

    override fun render(): Bitmap {
        val bitmap = Bitmap.createBitmap(
            DialGeometry.canvasSize,
            DialGeometry.canvasSize,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val status = statusSource.statusAt(clock.instant())

        drawMoonPhase(canvas, paint, status.moonPhase.kind)
        drawBattery(canvas, paint, status.battery)
        drawDate(canvas, paint, status.dateLabel)

        return bitmap
    }

    private fun drawMoonPhase(canvas: Canvas, paint: Paint, kind: MoonPhaseKind) {
        when (kind) {
            MoonPhaseKind.NEW -> drawNewMoon(canvas, paint)
            MoonPhaseKind.WAXING_CRESCENT -> drawCrescent(canvas, paint, litRight = true)
            MoonPhaseKind.FIRST_QUARTER -> drawHalfMoon(canvas, paint, litRight = true)
            MoonPhaseKind.WAXING_GIBBOUS -> drawGibbous(canvas, paint, litRight = true)
            MoonPhaseKind.FULL -> drawFullMoon(canvas, paint)
            MoonPhaseKind.WANING_GIBBOUS -> drawGibbous(canvas, paint, litRight = false)
            MoonPhaseKind.LAST_QUARTER -> drawHalfMoon(canvas, paint, litRight = false)
            MoonPhaseKind.WANING_CRESCENT -> drawCrescent(canvas, paint, litRight = false)
        }

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

    private fun drawFullMoon(canvas: Canvas, paint: Paint) {
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(235, 238, 242)
        canvas.drawCircle(MOON_CENTER_X, MOON_CENTER_Y, MOON_RADIUS, paint)
    }

    private fun drawHalfMoon(canvas: Canvas, paint: Paint, litRight: Boolean) {
        drawNewMoon(canvas, paint)
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

    private fun drawCrescent(canvas: Canvas, paint: Paint, litRight: Boolean) {
        drawFullMoon(canvas, paint)
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(8, 8, 10)
        val shadowOffset = if (litRight) -MOON_RADIUS * 0.45f else MOON_RADIUS * 0.45f
        canvas.drawCircle(MOON_CENTER_X + shadowOffset, MOON_CENTER_Y, MOON_RADIUS * 0.98f, paint)
    }

    private fun drawGibbous(canvas: Canvas, paint: Paint, litRight: Boolean) {
        drawFullMoon(canvas, paint)
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(8, 8, 10)
        val shadowOffset = if (litRight) -MOON_RADIUS * 1.25f else MOON_RADIUS * 1.25f
        canvas.drawCircle(MOON_CENTER_X + shadowOffset, MOON_CENTER_Y, MOON_RADIUS * 0.98f, paint)
    }

    private fun drawBattery(canvas: Canvas, paint: Paint, battery: BatteryStatus) {
        val color = if (battery.isLow) LOW_BATTERY_COLOR else STATUS_TEXT_COLOR
        val fillWidth = BATTERY_FILL_WIDTH * ((battery.percent ?: 0) / 100f)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.4f
        paint.color = color
        canvas.drawRoundRect(BATTERY_BODY_BOUNDS, 2f, 2f, paint)

        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(BATTERY_TERMINAL_BOUNDS, 1f, 1f, paint)

        if (battery.percent != null && fillWidth > 0f) {
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

    private companion object {
        private const val TOP_STATUS_Y = 52f
        private const val SIDE_STATUS_OFFSET_X = 42f
        private const val MOON_CENTER_X = DialGeometry.center - SIDE_STATUS_OFFSET_X
        private const val MOON_CENTER_Y = TOP_STATUS_Y
        private const val MOON_RADIUS = 10f
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
        private val STATUS_TEXT_COLOR = Color.rgb(245, 245, 242)
        private val LOW_BATTERY_COLOR = Color.rgb(229, 57, 53)
    }
}
