package com.derfence.astroface.wear.astro

import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.Observer
import io.github.cosinekitty.astronomy.Time
import java.time.Instant

internal fun AstroObserver.toAstronomyObserver(): Observer =
    Observer(latitude, longitude, elevationMeters)

internal fun Instant.toAstronomyTime(): Time =
    Time.fromMillisecondsSince1970(toEpochMilli())

internal fun Time.toInstant(): Instant =
    Instant.ofEpochMilli(toMillisecondsSince1970())

internal fun CelestialBody.toAstronomyBody(): Body =
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

internal fun Double.normalizedDegrees(): Double {
    val remainder = this % FULL_CIRCLE_DEGREES
    return if (remainder < 0.0) remainder + FULL_CIRCLE_DEGREES else remainder
}

private const val FULL_CIRCLE_DEGREES = 360.0
