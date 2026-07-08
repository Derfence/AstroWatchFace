package com.derfence.astroface.wear.astro

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AstronomyEngineConstellationSourceTest {
    @Test
    fun constellationsUseNextMidnightAfterSunrise() {
        val source = sourceWithSixAmRefresh()

        val snapshot = source.constellationsAt(
            Instant.parse("2026-07-08T06:00:00Z"),
            AstroObserver.DEFAULT
        )

        assertEquals(Instant.parse("2026-07-08T04:00:00Z"), snapshot.refreshInstant)
        assertEquals(Instant.parse("2026-07-08T22:00:00Z"), snapshot.targetMidnight)
        assertFalse(snapshot.lines.isEmpty())
    }

    @Test
    fun linesContainAtLeastOneEndpointInsideNinetyDegreesFromZenith() {
        val snapshot = sourceWithSixAmRefresh().constellationsAt(
            Instant.parse("2026-07-08T06:00:00Z"),
            AstroObserver.DEFAULT
        )

        snapshot.lines.forEach { line ->
            assertTrue(line.from.azimuthDegrees >= 0.0)
            assertTrue(line.from.azimuthDegrees < 360.0)
            assertTrue(line.to.azimuthDegrees >= 0.0)
            assertTrue(line.to.azimuthDegrees < 360.0)
            assertTrue(line.from.zenithDistanceDegrees <= 90.0 || line.to.zenithDistanceDegrees <= 90.0)
        }
    }

    @Test
    fun repeatedCallsDuringSameRefreshWindowKeepSameSky() {
        val sunriseProvider = CountingSixAmSunriseProvider()
        val source = sourceWithSunriseProvider(sunriseProvider)
        val first = source.constellationsAt(
            Instant.parse("2026-07-08T06:00:00Z"),
            AstroObserver.DEFAULT
        )
        val second = source.constellationsAt(
            Instant.parse("2026-07-08T20:00:00Z"),
            AstroObserver.DEFAULT
        )

        assertEquals(first.refreshInstant, second.refreshInstant)
        assertEquals(first.targetMidnight, second.targetMidnight)
        assertEquals(first.lines, second.lines)
        assertEquals(2, sunriseProvider.callCount)
    }

    private fun sourceWithSixAmRefresh(): AstronomyEngineConstellationSource =
        sourceWithSunriseProvider(SixAmSunriseProvider)

    private fun sourceWithSunriseProvider(provider: SunriseProvider): AstronomyEngineConstellationSource =
        AstronomyEngineConstellationSource(
            catalog = DefaultConstellationCatalog.value,
            refreshPolicy = ConstellationRefreshPolicy(provider)
        )

    private object SixAmSunriseProvider : SunriseProvider {
        override fun sunriseOn(date: LocalDate, observer: AstroObserver): Instant =
            date.atTime(LocalTime.of(6, 0))
                .atZone(observer.zoneId)
                .toInstant()
    }

    private class CountingSixAmSunriseProvider : SunriseProvider {
        var callCount = 0
            private set

        override fun sunriseOn(date: LocalDate, observer: AstroObserver): Instant {
            callCount += 1
            return SixAmSunriseProvider.sunriseOn(date, observer)
        }
    }
}
