package com.derfence.astroface.wear.dial

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.SweepGradient
import android.graphics.Typeface
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

class CelestialOverlayRenderer(
    private val clock: Clock = Clock.system(AstroObserver.DEFAULT.zoneId),
    private val observer: AstroObserver = AstroObserver.DEFAULT,
    private val positionSource: CelestialPositionSource = AstronomyEngineCelestialPositionSource(),
    private val horizonSource: CelestialHorizonSource = AstronomyEngineCelestialHorizonSource()
) : DialRenderer {
    override val contentDescription = "Positions célestes AstroFace"

    override fun render(): Bitmap {
        val bitmap = Bitmap.createBitmap(
            DialGeometry.canvasSize,
            DialGeometry.canvasSize,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val now = clock.instant()
        val snapshot = positionSource.positionsAt(now, observer)
        val horizonSnapshot = horizonSource.horizonMarkersAt(now, observer)

        drawSkyRing(canvas, paint)
        drawCompassLabels(canvas, paint)
        drawBodies(canvas, paint, snapshot.positions, horizonSnapshot.markers)

        return bitmap
    }

    private fun drawSkyRing(canvas: Canvas, paint: Paint) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.0f
        paint.color = Color.argb(115, 255, 255, 255)
        canvas.drawCircle(DialGeometry.center, DialGeometry.center, COMPASS_LABEL_RADIUS+8, paint)
    }

    private fun drawCompassLabels(canvas: Canvas, paint: Paint) {
        paint.style = Paint.Style.FILL
        paint.color = Color.argb(185, 255, 255, 255)
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.textSize = 12f

        COMPASS_LABELS.forEach { label ->
            val point = DialGeometry.point(COMPASS_LABEL_RADIUS, label.angleDegrees)
            canvas.drawText(label.text, point.x, point.y + 4f, paint)
        }
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
        val baseColor = tailColor(placement.body).withAlpha(255)

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
        paint.color = tailColor(placement.body).withAlpha(255)

        markers.forEach { marker ->
            val markerAngle = DialGeometry.angleForAzimuth(marker.azimuthDegrees)
            val inner = DialGeometry.point(placement.radius - HORIZON_TICK_LENGTH / 2f, markerAngle)
            val outer = DialGeometry.point(placement.radius + HORIZON_TICK_LENGTH / 2f, markerAngle)
            canvas.drawLine(inner.x, inner.y, outer.x, outer.y, paint)
        }
    }

    private fun drawOrbitTail(canvas: Canvas, paint: Paint, placement: OrbitPlacement) {
        val bounds = RectF(
            DialGeometry.center - placement.radius,
            DialGeometry.center - placement.radius,
            DialGeometry.center + placement.radius,
            DialGeometry.center + placement.radius
        )
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 0.5f
        val baseColor = tailColor(placement.body)
        val tailStart = placement.angleDegrees - CANVAS_ARC_OFFSET_DEGREES - TAIL_SWEEP_DEGREES
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

    private fun drawBodyIcon(
        canvas: Canvas,
        paint: Paint,
        body: CelestialBody,
        x: Float,
        y: Float
    ) {
        paint.style = Paint.Style.FILL
        paint.color = Color.argb(215, 0, 0, 0)
        canvas.drawCircle(x, y, 8.8f, paint)

        when (body) {
            CelestialBody.SUN -> drawSun(canvas, paint, x, y)
            CelestialBody.MOON -> drawMoon(canvas, paint, x, y)
            CelestialBody.MERCURY -> drawMercury(canvas, paint, x, y)
            CelestialBody.VENUS -> drawVenus(canvas, paint, x, y)
            CelestialBody.MARS -> drawMars(canvas, paint, x, y)
            CelestialBody.JUPITER -> drawJupiter(canvas, paint, x, y)
            CelestialBody.SATURN -> drawSaturn(canvas, paint, x, y)
            CelestialBody.URANUS -> drawUranus(canvas, paint, x, y)
            CelestialBody.NEPTUNE -> drawNeptune(canvas, paint, x, y)
        }
    }

    private fun drawSun(canvas: Canvas, paint: Paint, x: Float, y: Float) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.4f
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = Color.rgb(255, 204, 82)
        repeat(8) { index ->
            val angle = index * 45f
            val inner = DialGeometry.pointAround(x, y, 5.4f, angle)
            val outer = DialGeometry.pointAround(x, y, 7.4f, angle)
            canvas.drawLine(inner.x, inner.y, outer.x, outer.y, paint)
        }

        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(255, 224, 112)
        canvas.drawCircle(x, y, 4.8f, paint)
    }

    private fun drawMoon(canvas: Canvas, paint: Paint, x: Float, y: Float) {
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(235, 238, 242)
        canvas.drawCircle(x, y, 5.4f, paint)
        paint.color = Color.rgb(0, 0, 0)
        canvas.drawCircle(x + 2.5f, y - 0.4f, 5.1f, paint)
    }

    private fun drawMercury(canvas: Canvas, paint: Paint, x: Float, y: Float) {
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(166, 145, 119)
        canvas.drawCircle(x, y, 4.9f, paint)

        paint.color = Color.rgb(108, 96, 84)
        canvas.drawCircle(x - 1.6f, y - 1.0f, 0.9f, paint)
        canvas.drawCircle(x + 1.5f, y + 1.1f, 0.7f, paint)
    }

    private fun drawVenus(canvas: Canvas, paint: Paint, x: Float, y: Float) {
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(255, 188, 48)
        canvas.drawCircle(x, y, 5.7f, paint)

        paint.color = Color.rgb(0, 0, 0)
        canvas.drawCircle(x + 2.6f, y - 0.2f, 5.5f, paint)

        paint.color = Color.rgb(255, 228, 104)
        canvas.drawCircle(x - 1.8f, y - 1.8f, 1.1f, paint)
    }

    private fun drawMars(canvas: Canvas, paint: Paint, x: Float, y: Float) {
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(214, 68, 48)
        canvas.drawCircle(x, y, 5.2f, paint)

        paint.color = Color.rgb(122, 43, 34)
        canvas.drawCircle(x - 1.6f, y + 1.1f, 1.2f, paint)

        paint.color = Color.rgb(248, 230, 208)
        canvas.drawOval(RectF(x - 2.2f, y - 5.1f, x + 2.2f, y - 2.8f), paint)
    }

    private fun drawJupiter(canvas: Canvas, paint: Paint, x: Float, y: Float) {
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(229, 169, 98)
        canvas.drawCircle(x, y, 5.8f, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f
        paint.color = Color.rgb(255, 222, 166)
        canvas.drawLine(x - 4.8f, y - 1.6f, x + 4.8f, y - 1.6f, paint)
        canvas.drawLine(x - 4.2f, y + 1.8f, x + 4.2f, y + 1.8f, paint)
    }

    private fun drawSaturn(canvas: Canvas, paint: Paint, x: Float, y: Float) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.4f
        paint.color = Color.rgb(238, 215, 142)
        canvas.save()
        canvas.rotate(-18f, x, y)
        canvas.drawOval(RectF(x - 8.5f, y - 3.2f, x + 8.5f, y + 3.2f), paint)
        canvas.restore()

        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(227, 199, 112)
        canvas.drawCircle(x, y, 4.7f, paint)
    }

    private fun drawUranus(canvas: Canvas, paint: Paint, x: Float, y: Float) {
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(104, 211, 210)
        canvas.drawCircle(x, y, 5.1f, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.1f
        paint.color = Color.rgb(179, 244, 241)
        canvas.drawOval(RectF(x - 3.4f, y - 6.2f, x + 3.4f, y + 6.2f), paint)
    }

    private fun drawNeptune(canvas: Canvas, paint: Paint, x: Float, y: Float) {
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(69, 113, 225)
        canvas.drawCircle(x, y, 5.2f, paint)

        paint.color = Color.rgb(126, 166, 255)
        canvas.drawCircle(x - 1.8f, y - 1.8f, 1.3f, paint)
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

    private fun tailColor(body: CelestialBody): Int =
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
                TAIL_SWEEP_DEGREES / 360,
                TAIL_SWEEP_DEGREES / 360 + TAIL_GRADIENT_CLOSE_FRACTION,
                1f
            )
        )
        gradient.setLocalMatrix(Matrix().apply {
            setRotate(tailStartDegrees, DialGeometry.center, DialGeometry.center)
        })
        return gradient
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

    private data class RelativePoint(val x: Float, val y: Float)

    private data class OrbitPlacement(
        val body: CelestialBody,
        val angleDegrees: Float,
        val radius: Float
    )

    private data class CompassLabel(
        val text: String,
        val angleDegrees: Float
    )

    private fun DialGeometry.pointAround(
        centerX: Float,
        centerY: Float,
        radius: Float,
        angleDegrees: Float
    ): RelativePoint {
        val point = point(radius, angleDegrees)
        return RelativePoint(
            x = centerX + point.x - center,
            y = centerY + point.y - center
        )
    }

    private companion object {
        private const val FIRST_ORBIT_RADIUS = 150f-8*5f
        private const val ORBIT_SPACING = 5f
        private const val COMPASS_LABEL_RADIUS = 158f
        private const val FULL_CIRCLE_DEGREES_FLOAT = 360f
        private const val CANVAS_ARC_OFFSET_DEGREES = 90f
        private const val TAIL_SWEEP_DEGREES = 100f
        private const val TAIL_GRADIENT_CLOSE_FRACTION = 0.001f
        private const val HORIZON_TICK_LENGTH = ORBIT_SPACING * 0.8f
        private const val HORIZON_TICK_STROKE_WIDTH = 2f
        private const val NEGATIVE_ALTITUDE_ARC_SWEEP_DEGREES = 14f
        private const val NEGATIVE_ALTITUDE_ARC_STROKE_WIDTH = 1f
        private const val NEGATIVE_ALTITUDE_GRADIENT_CLOSE_FRACTION = 0.001f
        private val COMPASS_LABELS = listOf(
            CompassLabel("S", 0f),
            CompassLabel("O", 90f),
            CompassLabel("N", 180f),
            CompassLabel("E", 270f)
        )
    }
}
