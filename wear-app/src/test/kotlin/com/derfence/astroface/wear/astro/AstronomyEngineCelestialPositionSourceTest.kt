package com.derfence.astroface.wear.astro

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AstronomyEngineCelestialPositionSourceTest {
    @Test
    fun positionsContainEveryDisplayedBodyInStableOrder() {
        val snapshot = AstronomyEngineCelestialPositionSource()
            .positionsAt(Instant.parse("2026-07-07T22:00:00Z"), AstroObserver.DEFAULT)

        assertEquals(CelestialBody.entries, snapshot.positions.map { it.body })
        assertEquals(Instant.parse("2026-07-07T22:00:00Z"), snapshot.calculatedAt)
    }

    @Test
    fun azimuthsAreFiniteAndNormalized() {
        val snapshot = AstronomyEngineCelestialPositionSource()
            .positionsAt(Instant.parse("2026-07-07T22:00:00Z"), AstroObserver.DEFAULT)

        snapshot.positions.forEach { position ->
            assertTrue(position.azimuthDegrees >= 0.0)
            assertTrue(position.azimuthDegrees < 360.0)
            assertTrue(!position.azimuthDegrees.isNaN())
        }
    }
}
