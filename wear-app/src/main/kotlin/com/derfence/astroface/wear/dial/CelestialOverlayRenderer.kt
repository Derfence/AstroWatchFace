package com.derfence.astroface.wear.dial

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.derfence.astroface.wear.astro.AstroObserver
import com.derfence.astroface.wear.astro.AstronomyEngineCelestialPositionSource
import com.derfence.astroface.wear.astro.CelestialBody
import com.derfence.astroface.wear.astro.CelestialPosition
import com.derfence.astroface.wear.astro.CelestialPositionSource
import java.time.Clock
import java.time.Instant

class CelestialOverlayRenderer(
    private val clock: Clock = Clock.system(AstroObserver.DEFAULT.zoneId),
    private val observer: AstroObserver = AstroObserver.DEFAULT,
    private val positionSource: CelestialPositionSource = AstronomyEngineCelestialPositionSource(),
    private val viewport: DialViewport = DialViewport.CELESTIAL
) : DialRenderer {
    override val contentDescription = "Positions célestes AstroFace"

    override fun render(): Bitmap = renderAt(clock.instant())

    override fun renderAt(instant: Instant): Bitmap {
        val bitmap = Bitmap.createBitmap(viewport.width, viewport.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        viewport.translate(canvas)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val snapshot = positionSource.positionsAt(instant, observer)

        drawBodies(canvas, paint, snapshot.positions)
        return bitmap
    }

    private fun drawBodies(
        canvas: Canvas,
        paint: Paint,
        positions: List<CelestialPosition>
    ) {
        positions.map { position ->
            OrbitPlacement(
                body = position.body,
                angleDegrees = DialGeometry.angleForAzimuth(position.azimuthDegrees),
                radius = CelestialOrbitGeometry.radiusFor(position.body)
            )
        }.forEach { placement ->
            OrbitTailPainter.draw(
                canvas = canvas,
                paint = paint,
                radius = placement.radius,
                angleDegrees = placement.angleDegrees,
                baseColor = OrbitTailPainter.colorFor(placement.body)
            )
            val point = DialGeometry.point(placement.radius, placement.angleDegrees)
            CelestialBodyIconPainter.draw(canvas, paint, placement.body, point.x, point.y)
        }
    }

    private data class OrbitPlacement(
        val body: CelestialBody,
        val angleDegrees: Float,
        val radius: Float
    )
}
