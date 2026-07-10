package com.derfence.astroface.wear.astro

import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.helioVector
import java.time.Instant
import kotlin.math.atan2

class AstronomyEngineSolarSystemPositionSource : SolarSystemPositionSource {
    override fun positionsAt(time: Instant): SolarSystemSnapshot {
        val astronomyTime = time.toAstronomyTime()
        val positions = SolarSystemBody.entries.map { body ->
            if (body == SolarSystemBody.SUN) {
                SolarSystemPosition(
                    body = body,
                    heliocentricLongitudeDegrees = 0.0,
                    distanceAu = 0.0
                )
            } else {
                val vector = helioVector(body.toAstronomyBody(), astronomyTime)
                SolarSystemPosition(
                    body = body,
                    heliocentricLongitudeDegrees = Math.toDegrees(atan2(vector.y, vector.x))
                        .normalizedDegrees(),
                    distanceAu = vector.length()
                )
            }
        }

        return SolarSystemSnapshot(
            calculatedAt = time,
            positions = positions
        )
    }
}

private fun SolarSystemBody.toAstronomyBody(): Body =
    when (this) {
        SolarSystemBody.SUN -> Body.Sun
        SolarSystemBody.MERCURY -> Body.Mercury
        SolarSystemBody.VENUS -> Body.Venus
        SolarSystemBody.EARTH -> Body.Earth
        SolarSystemBody.MARS -> Body.Mars
        SolarSystemBody.JUPITER -> Body.Jupiter
        SolarSystemBody.SATURN -> Body.Saturn
        SolarSystemBody.URANUS -> Body.Uranus
        SolarSystemBody.NEPTUNE -> Body.Neptune
    }
