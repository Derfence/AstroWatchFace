package com.derfence.astroface.wear.astro

import java.time.Duration
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class CachingAstroEventSourceTest {
    @Test
    fun repeatedWindowsOnSameLocalDateReuseThreeDayEphemeris() {
        val delegate = CountingAstroEventSource()
        val source = CachingAstroEventSource(delegate)

        source.eventsBetween(
            Instant.parse("2026-07-08T08:00:00Z"),
            Instant.parse("2026-07-09T08:00:00Z"),
            AstroObserver.DEFAULT
        )
        source.eventsBetween(
            Instant.parse("2026-07-08T08:10:00Z"),
            Instant.parse("2026-07-09T08:10:00Z"),
            AstroObserver.DEFAULT
        )

        assertEquals(1, delegate.eventSearchCount)
    }

    @Test
    fun observerChangeInvalidatesEphemeris() {
        val delegate = CountingAstroEventSource()
        val source = CachingAstroEventSource(delegate)
        val start = Instant.parse("2026-07-08T08:00:00Z")
        val end = start.plus(Duration.ofHours(24))

        source.eventsBetween(start, end, AstroObserver.DEFAULT)
        source.eventsBetween(
            start,
            end,
            AstroObserver.DEFAULT.copy(latitude = AstroObserver.DEFAULT.latitude + 1.0)
        )

        assertEquals(2, delegate.eventSearchCount)
    }

    @Test
    fun threeLocalDaysRespectDaylightSavingTransition() {
        val delegate = CountingAstroEventSource()
        val source = CachingAstroEventSource(delegate)

        source.eventsBetween(
            Instant.parse("2026-03-29T10:00:00Z"),
            Instant.parse("2026-03-30T10:00:00Z"),
            AstroObserver.DEFAULT
        )

        assertEquals(Duration.ofHours(71), Duration.between(delegate.lastStart, delegate.lastEnd))
    }

    private class CountingAstroEventSource : AstroEventSource {
        var eventSearchCount = 0
            private set
        lateinit var lastStart: Instant
            private set
        lateinit var lastEnd: Instant
            private set

        override fun eventsBetween(
            start: Instant,
            end: Instant,
            observer: AstroObserver
        ): List<AstroEvent> {
            eventSearchCount += 1
            lastStart = start
            lastEnd = end
            return emptyList()
        }

        override fun sunAltitudeDegrees(time: Instant, observer: AstroObserver): Double = 1.0

        override fun moonAltitudeDegrees(time: Instant, observer: AstroObserver): Double = -1.0
    }
}
