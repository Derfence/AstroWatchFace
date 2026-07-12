package com.derfence.astroface.wear.dial

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.SweepGradient
import com.derfence.astroface.wear.astro.AstroObserver
import com.derfence.astroface.wear.astro.AstronomyEngineCelestialHorizonSource
import com.derfence.astroface.wear.astro.AstronomyEngineCelestialPositionSource
import com.derfence.astroface.wear.astro.CelestialBody
import com.derfence.astroface.wear.astro.CelestialHorizonEventType
import com.derfence.astroface.wear.astro.CelestialHorizonMarker
import com.derfence.astroface.wear.astro.CelestialHorizonSource
import com.derfence.astroface.wear.astro.CelestialPosition
import com.derfence.astroface.wear.astro.CelestialPositionSource
import java.time.Clock
import java.time.Instant

class CelestialOverlayRenderer(
    private val clock: Clock = Clock.system(AstroObserver.DEFAULT.zoneId),
    private val observer: AstroObserver = AstroObserver.DEFAULT,
    private val positionSource: CelestialPositionSource = AstronomyEngineCelestialPositionSource(),
    private val horizonSource: CelestialHorizonSource = AstronomyEngineCelestialHorizonSource()
) : DialRenderer {
    override val contentDescription = "Positions célestes AstroFace"

    override fun render(): Bitmap = renderAt(clock.instant())

    override fun renderAt(instant: Instant): Bitmap {
        val bitmap = Bitmap.createBitmap(
            DialGeometry.canvasSize,
            DialGeometry.canvasSize,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val snapshot = positionSource.positionsAt(instant, observer)
        val horizonSnapshot = horizonSource.horizonMarkersAt(instant, observer)

        drawSkyRing(canvas, paint)
        drawBodies(canvas, paint, snapshot.positions, horizonSnapshot.markers)

        return bitmap
    }

    private fun drawSkyRing(canvas: Canvas, paint: Paint) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.0f
        paint.color = Color.argb(115, 255, 255, 255)
        canvas.drawCircle(DialGeometry.center, DialGeometry.center, SKY_RING_RADIUS, paint)
    }

    private fun drawBodies(
        canvas: Canvas,
        paint: Paint,
        positions: List<CelestialPosition>,
        horizonMarkers: List<CelestialHorizonMarker>
    ) {
        val placements = positions.map { position ->
            val body = position.body
            OrbitPlacement(
                body = body,
                angleDegrees = DialGeometry.angleForAzimuth(position.azimuthDegrees),
                radius = orbitRadiusFor(body)
            )
        }
        val markersByBody = horizonMarkers.groupBy { it.body }

        placements.forEach { placement ->
            drawNegativeAltitudeArcs(canvas, paint, placement, markersByBody[placement.body].orEmpty())
        }
        placements.forEach { placement ->
            drawHorizonMarkerTicks(canvas, paint, placement, markersByBody[placement.body].orEmpty())
        }
        placements.forEach { placement ->
            drawOrbitTail(canvas, paint, placement)
        }
        placements.forEach { placement ->
            val point = DialGeometry.point(placement.radius, placement.angleDegrees)
            drawBodyIcon(canvas, paint, placement.body, point.x, point.y)
        }
    }

    private fun drawNegativeAltitudeArcs(
        canvas: Canvas,
        paint: Paint,
        placement: OrbitPlacement,
        markers: List<CelestialHorizonMarker>
    ) {
        if (markers.isEmpty()) {
            return
        }

        val bounds = RectF(
            DialGeometry.center - placement.radius,
            DialGeometry.center - placement.radius,
            DialGeometry.center + placement.radius,
            DialGeometry.center + placement.radius
        )
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = NEGATIVE_ALTITUDE_ARC_STROKE_WIDTH
        val baseColor = OrbitTailPainter.colorFor(placement.body).withAlpha(255)

        markers.forEach { marker ->
            val markerAngle = DialGeometry.angleForAzimuth(marker.azimuthDegrees)
            val arcStart = when (marker.type) {
                CelestialHorizonEventType.RISE ->
                    markerAngle - CANVAS_ARC_OFFSET_DEGREES - NEGATIVE_ALTITUDE_ARC_SWEEP_DEGREES
                CelestialHorizonEventType.SET ->
                    markerAngle - CANVAS_ARC_OFFSET_DEGREES
            }
            paint.shader = negativeAltitudeArcGradient(baseColor, arcStart, marker.type)
            canvas.drawArc(
                bounds,
                arcStart,
                NEGATIVE_ALTITUDE_ARC_SWEEP_DEGREES,
                false,
                paint
            )
        }
        paint.shader = null
    }

    private fun drawHorizonMarkerTicks(
        canvas: Canvas,
        paint: Paint,
        placement: OrbitPlacement,
        markers: List<CelestialHorizonMarker>
    ) {
        if (markers.isEmpty()) {
            return
        }

        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = HORIZON_TICK_STROKE_WIDTH
        paint.color = OrbitTailPainter.colorFor(placement.body).withAlpha(255)

        markers.forEach { marker ->
            val markerAngle = DialGeometry.angleForAzimuth(marker.azimuthDegrees)
            val inner = DialGeometry.point(placement.radius - HORIZON_TICK_LENGTH / 2f, markerAngle)
            val outer = DialGeometry.point(placement.radius + HORIZON_TICK_LENGTH / 2f, markerAngle)
            canvas.drawLine(inner.x, inner.y, outer.x, outer.y, paint)
        }
    }

    private fun drawOrbitTail(canvas: Canvas, paint: Paint, placement: OrbitPlacement) {
        OrbitTailPainter.draw(
            canvas = canvas,
            paint = paint,
            radius = placement.radius,
            angleDegrees = placement.angleDegrees,
            baseColor = OrbitTailPainter.colorFor(placement.body)
        )
    }

    private fun drawBodyIcon(
        canvas: Canvas,
        paint: Paint,
        body: CelestialBody,
        x: Float,
        y: Float
    ) {
        CelestialBodyIconPainter.draw(canvas, paint, body, x, y)
    }

    private fun orbitRadiusFor(body: CelestialBody): Float =
        FIRST_ORBIT_RADIUS + ORBIT_SPACING * orbitIndexFor(body)

    private fun orbitIndexFor(body: CelestialBody): Int =
        when (body) {
            CelestialBody.SUN -> 0
            CelestialBody.MOON -> 1
            CelestialBody.MERCURY -> 2
            CelestialBody.VENUS -> 3
            CelestialBody.MARS -> 4
            CelestialBody.JUPITER -> 5
            CelestialBody.SATURN -> 6
            CelestialBody.URANUS -> 7
            CelestialBody.NEPTUNE -> 8
        }

    private fun negativeAltitudeArcGradient(
        baseColor: Int,
        arcStartDegrees: Float,
        type: CelestialHorizonEventType
    ): SweepGradient {
        val transparent = baseColor.withAlpha(0)
        val arcFraction = NEGATIVE_ALTITUDE_ARC_SWEEP_DEGREES / FULL_CIRCLE_DEGREES_FLOAT
        val gradient = when (type) {
            CelestialHorizonEventType.RISE -> SweepGradient(
                DialGeometry.center,
                DialGeometry.center,
                intArrayOf(
                    transparent,
                    baseColor,
                    transparent,
                    transparent
                ),
                floatArrayOf(
                    0f,
                    arcFraction,
                    arcFraction + NEGATIVE_ALTITUDE_GRADIENT_CLOSE_FRACTION,
                    1f
                )
            )
            CelestialHorizonEventType.SET -> SweepGradient(
                DialGeometry.center,
                DialGeometry.center,
                intArrayOf(
                    baseColor,
                    transparent,
                    transparent,
                    transparent
                ),
                floatArrayOf(
                    0f,
                    arcFraction,
                    arcFraction + NEGATIVE_ALTITUDE_GRADIENT_CLOSE_FRACTION,
                    1f
                )
            )
        }
        gradient.setLocalMatrix(Matrix().apply {
            setRotate(arcStartDegrees, DialGeometry.center, DialGeometry.center)
        })
        return gradient
    }

    private fun Int.withAlpha(alpha: Int): Int =
        Color.argb(alpha, Color.red(this), Color.green(this), Color.blue(this))

    private data class OrbitPlacement(
        val body: CelestialBody,
        val angleDegrees: Float,
        val radius: Float
    )

    private companion object {
        private const val FIRST_ORBIT_RADIUS = 150f-8*5f
        private const val ORBIT_SPACING = 5f
        private const val SKY_RING_RADIUS = 166f
        private const val FULL_CIRCLE_DEGREES_FLOAT = 360f
        private const val CANVAS_ARC_OFFSET_DEGREES = 90f
        private const val HORIZON_TICK_LENGTH = ORBIT_SPACING * 0.8f
        private const val HORIZON_TICK_STROKE_WIDTH = 2f
        private const val NEGATIVE_ALTITUDE_ARC_SWEEP_DEGREES = 14f
        private const val NEGATIVE_ALTITUDE_ARC_STROKE_WIDTH = 1f
        private const val NEGATIVE_ALTITUDE_GRADIENT_CLOSE_FRACTION = 0.001f
    }
}
