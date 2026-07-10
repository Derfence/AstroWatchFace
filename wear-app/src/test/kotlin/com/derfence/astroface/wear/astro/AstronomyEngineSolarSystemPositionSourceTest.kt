package com.derfence.astroface.wear.astro

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AstronomyEngineSolarSystemPositionSourceTest {
    @Test
    fun positionsContainSunAndPlanetsIncludingEarth() {
        val snapshot = AstronomyEngineSolarSystemPositionSource()
            .positionsAt(Instant.parse("2026-07-07T22:00:00Z"))

        assertEquals(SolarSystemBody.entries, snapshot.positions.map { it.body })
        assertTrue(snapshot.positions.any { it.body == SolarSystemBody.EARTH })
        assertFalse(snapshot.positions.map { it.body.name }.contains("MOON"))
        assertEquals(Instant.parse("2026-07-07T22:00:00Z"), snapshot.calculatedAt)
    }

    @Test
    fun longitudesAreFiniteAndNormalized() {
        val snapshot = AstronomyEngineSolarSystemPositionSource()
            .positionsAt(Instant.parse("2026-07-07T22:00:00Z"))

        snapshot.positions.forEach { position ->
            assertTrue(position.heliocentricLongitudeDegrees >= 0.0)
            assertTrue(position.heliocentricLongitudeDegrees < 360.0)
            assertTrue(!position.heliocentricLongitudeDegrees.isNaN())
            assertTrue(!position.distanceAu.isNaN())
        }
    }
}
