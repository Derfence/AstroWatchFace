package com.derfence.astroface.wear.astro

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class CachingCelestialPositionSourceTest {
    @Test
    fun defaultCapacityCoversAFullUnalignedTenHourTimeline() {
        assertEquals(128, CachingCelestialPositionSource.DEFAULT_MAX_ENTRIES)

        val delegate = CountingPositionSource()
        val source = CachingCelestialPositionSource(delegate)
        val start = Instant.parse("2026-07-20T10:00:00Z")
        val instants = (0 until 62).map { start.plusSeconds(it * 600L) }

        instants.forEach { source.positionsAt(it, AstroObserver.DEFAULT) }
        instants.forEach { source.positionsAt(it, AstroObserver.DEFAULT) }

        assertEquals(62, delegate.calls)
    }

    @Test
    fun reusesAnObserverAndInstantPair() {
        val delegate = CountingPositionSource()
        val source = CachingCelestialPositionSource(delegate, maxEntries = 2)
        val instant = Instant.parse("2026-07-20T10:00:00Z")

        source.positionsAt(instant, AstroObserver.DEFAULT)
        source.positionsAt(instant, AstroObserver.DEFAULT)

        assertEquals(1, delegate.calls)
    }

    @Test
    fun observerChangeInvalidatesTheCacheKey() {
        val delegate = CountingPositionSource()
        val source = CachingCelestialPositionSource(delegate, maxEntries = 2)
        val instant = Instant.parse("2026-07-20T10:00:00Z")
        val otherObserver = AstroObserver.DEFAULT.copy(latitude = 46.0)

        source.positionsAt(instant, AstroObserver.DEFAULT)
        source.positionsAt(instant, otherObserver)

        assertEquals(2, delegate.calls)
    }

    private class CountingPositionSource : CelestialPositionSource {
        var calls = 0

        override fun positionsAt(
            time: Instant,
            observer: AstroObserver
        ): CelestialPositionSnapshot {
            calls += 1
            return CelestialPositionSnapshot(time, emptyList())
        }
    }
}
