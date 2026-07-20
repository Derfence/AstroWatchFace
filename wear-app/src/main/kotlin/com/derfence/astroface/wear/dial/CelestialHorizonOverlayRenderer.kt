package com.derfence.astroface.wear.dial

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.SweepGradient
import com.derfence.astroface.wear.astro.AstroObserver
import com.derfence.astroface.wear.astro.CelestialBody
import com.derfence.astroface.wear.astro.CelestialHorizonEventType
import com.derfence.astroface.wear.astro.CelestialHorizonMarker
import com.derfence.astroface.wear.astro.CelestialHorizonSource
import com.derfence.astroface.wear.astro.SharedAstronomySources
import com.derfence.astroface.wear.astro.SynchronizedLruCache
import java.time.Clock
import java.time.Instant

class CelestialHorizonOverlayRenderer(
    private val clock: Clock = Clock.system(AstroObserver.DEFAULT.zoneId),
    private val observer: AstroObserver = AstroObserver.DEFAULT,
    private val horizonSource: CelestialHorizonSource = SharedAstronomySources.celestialHorizonSource,
    private val viewport: DialViewport = DialViewport.CELESTIAL
) : DialRenderer {
    override val contentDescription = "Horizon céleste quotidien AstroFace"

    override fun render(): Bitmap = renderAt(clock.instant())

    override fun renderAt(instant: Instant): Bitmap = renderFrameAt(instant).bitmap

    override fun renderFrameAt(instant: Instant): RenderedDialFrame {
        val snapshot = horizonSource.horizonMarkersAt(instant, observer)
        val key = CacheKey(observer, horizonSource, snapshot.localDate, viewport)
        val bitmap = bitmapCache.getOrPut(key) {
            Bitmap.createBitmap(viewport.width, viewport.height, Bitmap.Config.ARGB_8888).also {
                val canvas = Canvas(it)
                viewport.translate(canvas)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                drawSkyRing(canvas, paint)
                CelestialBody.entries.forEach { body ->
                    val markers = snapshot.markers.filter { marker -> marker.body == body }
                    drawNegativeAltitudeArcs(canvas, paint, body, markers)
                    drawHorizonMarkerTicks(canvas, paint, body, markers)
                }
            }
        }
        val validUntil = snapshot.localDate.plusDays(1)
            .atStartOfDay(observer.zoneId)
            .toInstant()
        return RenderedDialFrame(bitmap, key, validUntil)
    }

    private fun drawSkyRing(canvas: Canvas, paint: Paint) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = Color.argb(115, 255, 255, 255)
        canvas.drawCircle(DialGeometry.center, DialGeometry.center, SKY_RING_RADIUS, paint)
    }

    private fun drawNegativeAltitudeArcs(
        canvas: Canvas,
        paint: Paint,
        body: CelestialBody,
        markers: List<CelestialHorizonMarker>
    ) {
        val radius = CelestialOrbitGeometry.radiusFor(body)
        val bounds = RectF(
            DialGeometry.center - radius,
            DialGeometry.center - radius,
            DialGeometry.center + radius,
            DialGeometry.center + radius
        )
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = NEGATIVE_ALTITUDE_ARC_STROKE_WIDTH
        val baseColor = OrbitTailPainter.colorFor(body).withAlpha(255)

        markers.forEach { marker ->
            val markerAngle = DialGeometry.angleForAzimuth(marker.azimuthDegrees)
            val arcStart = when (marker.type) {
                CelestialHorizonEventType.RISE ->
                    markerAngle - CANVAS_ARC_OFFSET_DEGREES - NEGATIVE_ALTITUDE_ARC_SWEEP_DEGREES
                CelestialHorizonEventType.SET -> markerAngle - CANVAS_ARC_OFFSET_DEGREES
            }
            paint.shader = negativeAltitudeArcGradient(baseColor, arcStart, marker.type)
            canvas.drawArc(bounds, arcStart, NEGATIVE_ALTITUDE_ARC_SWEEP_DEGREES, false, paint)
        }
        paint.shader = null
    }

    private fun drawHorizonMarkerTicks(
        canvas: Canvas,
        paint: Paint,
        body: CelestialBody,
        markers: List<CelestialHorizonMarker>
    ) {
        val radius = CelestialOrbitGeometry.radiusFor(body)
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = HORIZON_TICK_STROKE_WIDTH
        paint.color = OrbitTailPainter.colorFor(body).withAlpha(255)

        markers.forEach { marker ->
            val angle = DialGeometry.angleForAzimuth(marker.azimuthDegrees)
            val inner = DialGeometry.point(radius - HORIZON_TICK_LENGTH / 2f, angle)
            val outer = DialGeometry.point(radius + HORIZON_TICK_LENGTH / 2f, angle)
            canvas.drawLine(inner.x, inner.y, outer.x, outer.y, paint)
        }
    }

    private fun negativeAltitudeArcGradient(
        baseColor: Int,
        arcStartDegrees: Float,
        type: CelestialHorizonEventType
    ): SweepGradient {
        val transparent = baseColor.withAlpha(0)
        val arcFraction = NEGATIVE_ALTITUDE_ARC_SWEEP_DEGREES / 360f
        val colors = when (type) {
            CelestialHorizonEventType.RISE -> intArrayOf(transparent, baseColor, transparent, transparent)
            CelestialHorizonEventType.SET -> intArrayOf(baseColor, transparent, transparent, transparent)
        }
        val positions = floatArrayOf(
            0f,
            arcFraction,
            arcFraction + NEGATIVE_ALTITUDE_GRADIENT_CLOSE_FRACTION,
            1f
        )
        return SweepGradient(DialGeometry.center, DialGeometry.center, colors, positions).also {
            it.setLocalMatrix(Matrix().apply {
                setRotate(arcStartDegrees, DialGeometry.center, DialGeometry.center)
            })
        }
    }

    private fun Int.withAlpha(alpha: Int): Int =
        Color.argb(alpha, Color.red(this), Color.green(this), Color.blue(this))

    private data class CacheKey(
        val observer: AstroObserver,
        val source: CelestialHorizonSource,
        val localDate: java.time.LocalDate,
        val viewport: DialViewport
    )

    private companion object {
        private val bitmapCache = SynchronizedLruCache<CacheKey, Bitmap>(2)
        private const val SKY_RING_RADIUS = 166f
        private const val CANVAS_ARC_OFFSET_DEGREES = 90f
        private const val HORIZON_TICK_LENGTH = CelestialOrbitGeometry.ORBIT_SPACING * 0.8f
        private const val HORIZON_TICK_STROKE_WIDTH = 2f
        private const val NEGATIVE_ALTITUDE_ARC_SWEEP_DEGREES = 14f
        private const val NEGATIVE_ALTITUDE_ARC_STROKE_WIDTH = 1f
        private const val NEGATIVE_ALTITUDE_GRADIENT_CLOSE_FRACTION = 0.001f
    }
}
