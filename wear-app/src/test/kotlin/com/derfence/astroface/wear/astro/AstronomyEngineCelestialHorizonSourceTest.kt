package com.derfence.astroface.wear.astro

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AstronomyEngineCelestialHorizonSourceTest {
    @Test
    fun horizonMarkersUseObserverLocalDate() {
        val snapshot = AstronomyEngineCelestialHorizonSource()
            .horizonMarkersAt(Instant.parse("2026-07-09T22:30:00Z"), AstroObserver.DEFAULT)

        assertEquals(LocalDate.of(2026, 7, 10), snapshot.localDate)
        assertEquals(Instant.parse("2026-07-09T22:30:00Z"), snapshot.calculatedAt)
    }

    @Test
    fun sunRiseOnJulyTenthUsesLocalTimeAndExpectedAzimuth() {
        val snapshot = AstronomyEngineCelestialHorizonSource()
            .horizonMarkersAt(Instant.parse("2026-07-10T05:00:00Z"), AstroObserver.DEFAULT)
        val sunrise = snapshot.markers.single {
            it.body == CelestialBody.SUN && it.type == CelestialHorizonEventType.RISE
        }

        assertEquals(
            LocalTime.of(6, 8),
            sunrise.time
                .atZone(AstroObserver.DEFAULT.zoneId)
                .toLocalTime()
                .truncatedTo(ChronoUnit.MINUTES)
        )
        assertEquals(56.2, sunrise.azimuthDegrees, 0.5)
    }

    @Test
    fun markersStayInsideRequestedLocalDateWithNormalizedAzimuths() {
        val snapshot = AstronomyEngineCelestialHorizonSource()
            .horizonMarkersAt(Instant.parse("2026-07-10T05:00:00Z"), AstroObserver.DEFAULT)

        assertTrue(snapshot.markers.isNotEmpty())
        snapshot.markers.forEach { marker ->
            assertEquals(
                snapshot.localDate,
                marker.time.atZone(AstroObserver.DEFAULT.zoneId).toLocalDate()
            )
            assertTrue(marker.azimuthDegrees >= 0.0)
            assertTrue(marker.azimuthDegrees < 360.0)
            assertTrue(!marker.azimuthDegrees.isNaN())
        }
    }
}
