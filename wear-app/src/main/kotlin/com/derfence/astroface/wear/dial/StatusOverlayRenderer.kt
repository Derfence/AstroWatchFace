package com.derfence.astroface.wear.dial

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.derfence.astroface.wear.astro.AstroObserver
import com.derfence.astroface.wear.status.WatchStatusSource
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class StatusOverlayRenderer(
    private val statusSource: WatchStatusSource,
    private val moonSurface: Bitmap,
    private val clock: Clock = Clock.system(AstroObserver.DEFAULT.zoneId),
    private val zoneId: ZoneId = AstroObserver.DEFAULT.zoneId,
    private val viewport: DialViewport = DialViewport.STATUS
) : DialRenderer {
    override val contentDescription = "Date et phase de Lune AstroFace"

    override fun render(): Bitmap = renderAt(clock.instant())

    override fun renderAt(instant: Instant): Bitmap = renderFrameAt(instant).bitmap

    override fun renderFrameAt(instant: Instant): RenderedDialFrame {
        val status = statusSource.statusAt(instant)
        val bitmap = Bitmap.createBitmap(
            viewport.width,
            viewport.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        viewport.translate(canvas)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        drawMoonPhase(canvas, paint, status.moonPhase.phaseAngleDegrees)
        drawDate(canvas, paint, status.dateLabel)

        val nextMidnight = instant.atZone(zoneId).toLocalDate()
            .plusDays(1)
            .atStartOfDay(zoneId)
            .toInstant()
        val validUntil = status.moonPhase.validUntil
            ?.let { minOf(it, nextMidnight) }
            ?: nextMidnight
        return RenderedDialFrame(
            bitmap = bitmap,
            contentKey = ContentKey(
                dateLabel = status.dateLabel,
                moonTarget = status.moonPhase.targetTime,
                phaseAngleDegrees = status.moonPhase.phaseAngleDegrees
            ),
            validUntil = validUntil
        )
    }

    private fun drawMoonPhase(canvas: Canvas, paint: Paint, phaseAngleDegrees: Double) {
        MoonPhasePainter.draw(
            canvas = canvas,
            paint = paint,
            surface = moonSurface,
            centerX = MOON_CENTER_X,
            centerY = MOON_CENTER_Y,
            radius = MOON_RADIUS,
            phaseAngleDegrees = phaseAngleDegrees
        )
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
        private val STATUS_TEXT_COLOR = Color.rgb(245, 245, 242)
    }

    private data class ContentKey(
        val dateLabel: String,
        val moonTarget: Instant,
        val phaseAngleDegrees: Double
    )
}
