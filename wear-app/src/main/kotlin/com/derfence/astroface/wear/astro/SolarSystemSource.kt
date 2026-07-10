package com.derfence.astroface.wear.astro

import java.time.Instant

enum class SolarSystemBody {
    SUN,
    MERCURY,
    VENUS,
    EARTH,
    MARS,
    JUPITER,
    SATURN,
    URANUS,
    NEPTUNE
}

data class SolarSystemPosition(
    val body: SolarSystemBody,
    val heliocentricLongitudeDegrees: Double,
    val distanceAu: Double
)

data class SolarSystemSnapshot(
    val calculatedAt: Instant,
    val positions: List<SolarSystemPosition>
)

interface SolarSystemPositionSource {
    fun positionsAt(time: Instant): SolarSystemSnapshot
}
