package com.derfence.astroface.wear.astro

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MoonPhaseSourceTest {
    @Test
    fun phaseAnglesMapToEightDisplayBuckets() {
        assertEquals(MoonPhaseKind.NEW, MoonPhaseKind.fromAngleDegrees(0.0))
        assertEquals(MoonPhaseKind.WAXING_CRESCENT, MoonPhaseKind.fromAngleDegrees(23.0))
        assertEquals(MoonPhaseKind.FIRST_QUARTER, MoonPhaseKind.fromAngleDegrees(68.0))
        assertEquals(MoonPhaseKind.WAXING_GIBBOUS, MoonPhaseKind.fromAngleDegrees(113.0))
        assertEquals(MoonPhaseKind.FULL, MoonPhaseKind.fromAngleDegrees(158.0))
        assertEquals(MoonPhaseKind.WANING_GIBBOUS, MoonPhaseKind.fromAngleDegrees(203.0))
        assertEquals(MoonPhaseKind.LAST_QUARTER, MoonPhaseKind.fromAngleDegrees(248.0))
        assertEquals(MoonPhaseKind.WANING_CRESCENT, MoonPhaseKind.fromAngleDegrees(293.0))
        assertEquals(MoonPhaseKind.NEW, MoonPhaseKind.fromAngleDegrees(338.0))
    }

    @Test
    fun phaseAnglesAreNormalizedBeforeMapping() {
        assertEquals(MoonPhaseKind.NEW, MoonPhaseKind.fromAngleDegrees(-5.0))
        assertEquals(MoonPhaseKind.NEW, MoonPhaseKind.fromAngleDegrees(720.0))
        assertEquals(MoonPhaseKind.WANING_CRESCENT, MoonPhaseKind.fromAngleDegrees(-45.0))
    }

    @Test
    fun astronomyEnginePhaseSnapshotIsNormalized() {
        val snapshot = AstronomyEngineMoonPhaseSource()
            .phaseAt(Instant.parse("2026-07-07T22:00:00Z"))

        assertEquals(Instant.parse("2026-07-07T22:00:00Z"), snapshot.calculatedAt)
        assertTrue(snapshot.phaseAngleDegrees >= 0.0)
        assertTrue(snapshot.phaseAngleDegrees < 360.0)
        assertTrue(snapshot.illuminationPercent in 0..100)
    }
}
