package com.derfence.astroface.wear.dial

import com.derfence.astroface.wear.astro.CelestialBody

object CelestialOrbitGeometry {
    fun radiusFor(body: CelestialBody): Float =
        FIRST_ORBIT_RADIUS + ORBIT_SPACING * when (body) {
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

    const val ORBIT_SPACING = 5f
    private const val FIRST_ORBIT_RADIUS = 110f
}
