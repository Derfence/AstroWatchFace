package com.derfence.astroface.wear.dial

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.derfence.astroface.wear.astro.AstroEvent
import com.derfence.astroface.wear.astro.AstroEventType
import com.derfence.astroface.wear.astro.AstroInterval
import com.derfence.astroface.wear.astro.AstroIntervalType
import com.derfence.astroface.wear.astro.AstroObserver
import com.derfence.astroface.wear.astro.AstroWindowCalculator
import com.derfence.astroface.wear.astro.RollingAstroWindow
import java.time.Clock

class Dial24hRenderer(
    private val clock: Clock = Clock.system(AstroObserver.DEFAULT.zoneId),
    private val observer: AstroObserver = AstroObserver.DEFAULT,
    private val astroWindowCalculator: AstroWindowCalculator = AstroWindowCalculator()
) : DialRenderer {
    override val contentDescription = "Cadran AstroFace 24 heures"

    override fun render(): Bitmap {
        val bitmap = Bitmap.createBitmap(
            DialGeometry.canvasSize,
            DialGeometry.canvasSize,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val window = astroWindowCalculator.calculate(clock.instant(), observer)

        drawAstroIntervals(canvas, paint, window)

        paint.style = Paint.Style.STROKE
        paint.color = Color.argb(210, 120, 12, 20)
        paint.strokeWidth = 2.5f
        canvas.drawCircle(DialGeometry.center, DialGeometry.center, 216f, paint)

        drawTicks(canvas, paint)
        drawLabels(canvas, paint)
        drawNowSeparator(canvas, paint, window)
        drawEventMarkers(canvas, paint, window.events)

        return bitmap
    }

    private fun drawAstroIntervals(canvas: Canvas, paint: Paint, window: RollingAstroWindow) {
        window.intervals
            .sortedByDescending { intervalStyle(it.type).radius }
            .forEach { interval ->
                val style = intervalStyle(interval.type)
                paint.style = Paint.Style.STROKE
                paint.strokeCap = Paint.Cap.BUTT
                paint.color = style.color
                paint.strokeWidth = style.strokeWidth
                drawIntervalArc(canvas, paint, interval, style.radius)
            }
    }

    private fun drawIntervalArc(
        canvas: Canvas,
        paint: Paint,
        interval: AstroInterval,
        radius: Float
    ) {
        val bounds = RectF(
            DialGeometry.center - radius,
            DialGeometry.center - radius,
            DialGeometry.center + radius,
            DialGeometry.center + radius
        )
        DialGeometry.arcSegmentsFor(interval.start, interval.end, observer.zoneId).forEach { segment ->
            canvas.drawArc(
                bounds,
                segment.startAngleDegrees - CANVAS_ARC_OFFSET_DEGREES,
                segment.sweepDegrees,
                false,
                paint
            )
        }
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

    private fun drawNowSeparator(canvas: Canvas, paint: Paint, window: RollingAstroWindow) {
        val angle = DialGeometry.angleForInstant(window.start, observer.zoneId)

        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.SQUARE

        paint.color = Color.BLACK
        paint.strokeWidth = 12f
        drawRadialLine(canvas, paint, angle, 184f, 222f)

        paint.color = Color.argb(240, 229, 57, 53)
        paint.strokeWidth = 3.5f
        drawRadialLine(canvas, paint, angle, 186f, 222f)

        paint.color = Color.argb(235, 255, 255, 255)
        paint.strokeWidth = 1.3f
        drawRadialLine(canvas, paint, angle, 192f, 218f)
    }

    private fun drawEventMarkers(canvas: Canvas, paint: Paint, events: List<AstroEvent>) {
        paint.style = Paint.Style.FILL
        paint.strokeCap = Paint.Cap.ROUND

        events.filterNot { it.isSunEndpoint() }
            .forEach { drawEventMarker(canvas, paint, it) }
        events.filter { it.isSunEndpoint() }
            .forEach { drawEventMarker(canvas, paint, it) }
    }

    private fun drawEventMarker(canvas: Canvas, paint: Paint, event: AstroEvent) {
        val style = eventStyle(event.type)
        val point = DialGeometry.point(
            radius = style.radius,
            angleDegrees = DialGeometry.angleForInstant(event.time, observer.zoneId)
        )
        paint.color = Color.BLACK
        canvas.drawCircle(point.x, point.y, style.size + 1.8f, paint)
        paint.color = style.color
        canvas.drawCircle(point.x, point.y, style.size, paint)
    }

    private fun drawRadialLine(
        canvas: Canvas,
        paint: Paint,
        angle: Float,
        innerRadius: Float,
        outerRadius: Float
    ) {
        val inner = DialGeometry.point(innerRadius, angle)
        val outer = DialGeometry.point(outerRadius, angle)
        canvas.drawLine(inner.x, inner.y, outer.x, outer.y, paint)
    }

    private fun intervalStyle(type: AstroIntervalType): ArcStyle =
        when (type) {
            AstroIntervalType.SUNLIGHT -> ArcStyle(
                radius = SUN_TRACK_RADIUS,
                strokeWidth = 8f,
                color = Color.argb(210, 255, 180, 64)
            )
            AstroIntervalType.ASTRONOMICAL_TWILIGHT -> ArcStyle(
                radius = SUN_TRACK_RADIUS,
                strokeWidth = 8f,
                color = Color.argb(205, 67, 120, 210)
            )
            AstroIntervalType.ASTRONOMICAL_NIGHT -> ArcStyle(
                radius = SUN_TRACK_RADIUS,
                strokeWidth = 8f,
                color = Color.argb(205, 40, 52, 118)
            )
            AstroIntervalType.MOON_VISIBLE -> ArcStyle(
                radius = MOON_TRACK_RADIUS,
                strokeWidth = 5f,
                color = Color.argb(220, 232, 232, 220)
            )
        }

    private fun eventStyle(type: AstroEventType): MarkerStyle =
        when (type) {
            AstroEventType.SUNRISE,
            AstroEventType.SUNSET -> MarkerStyle(
                radius = SUN_TRACK_RADIUS,
                size = 4.5f,
                color = Color.argb(255, 255, 205, 88)
            )
            AstroEventType.ASTRONOMICAL_DAWN,
            AstroEventType.ASTRONOMICAL_DUSK -> MarkerStyle(
                radius = SUN_TRACK_RADIUS,
                size = 3.8f,
                color = Color.argb(255, 108, 156, 235)
            )
            AstroEventType.MOONRISE,
            AstroEventType.MOONSET -> MarkerStyle(
                radius = MOON_TRACK_RADIUS,
                size = 3.8f,
                color = Color.argb(255, 245, 245, 232)
            )
        }

    private data class ArcStyle(
        val radius: Float,
        val strokeWidth: Float,
        val color: Int
    )

    private data class MarkerStyle(
        val radius: Float,
        val size: Float,
        val color: Int
    )

    private fun AstroEvent.isSunEndpoint(): Boolean =
        type == AstroEventType.SUNRISE || type == AstroEventType.SUNSET

    companion object {
        private const val CANVAS_ARC_OFFSET_DEGREES = 90f
        private const val SUN_TRACK_RADIUS = 211f
        private const val MOON_TRACK_RADIUS = 200f
    }
}
