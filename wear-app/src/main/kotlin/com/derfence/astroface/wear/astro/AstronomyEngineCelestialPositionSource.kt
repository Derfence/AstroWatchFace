package com.derfence.astroface.wear.astro

import io.github.cosinekitty.astronomy.Aberration
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
}
