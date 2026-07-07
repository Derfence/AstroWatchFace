package com.derfence.astroface.wear.astro

import io.github.cosinekitty.astronomy.Aberration
import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.EquatorEpoch
import io.github.cosinekitty.astronomy.Refraction
import io.github.cosinekitty.astronomy.equator
import io.github.cosinekitty.astronomy.horizon
import java.time.Instant

class AstronomyEngineCelestialPositionSource : CelestialPositionSource {
    override fun positionsAt(
        time: Instant,
        observer: AstroObserver
    ): CelestialPositionSnapshot {
        val astronomyTime = time.toAstronomyTime()
        val astronomyObserver = observer.toAstronomyObserver()
        val positions = CelestialBody.entries.map { body ->
            val equatorial = equator(
                body.toAstronomyBody(),
                astronomyTime,
                astronomyObserver,
                EquatorEpoch.OfDate,
                Aberration.Corrected
            )
            val horizontal = horizon(
                astronomyTime,
                astronomyObserver,
                equatorial.ra,
                equatorial.dec,
                Refraction.None
            )
            CelestialPosition(
                body = body,
                azimuthDegrees = horizontal.azimuth.normalizedDegrees()
            )
        }

        return CelestialPositionSnapshot(
            calculatedAt = time,
            positions = positions
        )
    }

    private fun CelestialBody.toAstronomyBody(): Body =
        when (this) {
            CelestialBody.SUN -> Body.Sun
            CelestialBody.MOON -> Body.Moon
            CelestialBody.MERCURY -> Body.Mercury
            CelestialBody.VENUS -> Body.Venus
            CelestialBody.MARS -> Body.Mars
            CelestialBody.JUPITER -> Body.Jupiter
            CelestialBody.SATURN -> Body.Saturn
            CelestialBody.URANUS -> Body.Uranus
            CelestialBody.NEPTUNE -> Body.Neptune
        }

    private fun Double.normalizedDegrees(): Double {
        val remainder = this % FULL_CIRCLE_DEGREES
        return if (remainder < 0.0) remainder + FULL_CIRCLE_DEGREES else remainder
    }

    private companion object {
        private const val FULL_CIRCLE_DEGREES = 360.0
    }
}
