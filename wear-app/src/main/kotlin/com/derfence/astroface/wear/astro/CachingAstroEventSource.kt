package com.derfence.astroface.wear.astro

import java.time.Instant
import java.time.LocalDate

class CachingAstroEventSource(
    private val delegate: AstroEventSource = AstronomyEngineAstroEventSource()
) : AstroEventSource {
    private val cache = SynchronizedLruCache<CacheKey, CachedEphemeris>(MAX_CACHED_DATES)

    override fun eventsBetween(
        start: Instant,
        end: Instant,
        observer: AstroObserver
    ): List<AstroEvent> {
        val localDate = start.atZone(observer.zoneId).toLocalDate()
        val ephemeris = cache.getOrPut(CacheKey(observer, localDate)) {
            buildEphemeris(localDate, observer)
        }
        if (start.isBefore(ephemeris.start) || end.isAfter(ephemeris.end)) {
            return delegate.eventsBetween(start, end, observer)
        }
        return ephemeris.events.filter { !it.time.isBefore(start) && !it.time.isAfter(end) }
    }

    override fun sunAltitudeDegrees(time: Instant, observer: AstroObserver): Double =
        delegate.sunAltitudeDegrees(time, observer)

    override fun moonAltitudeDegrees(time: Instant, observer: AstroObserver): Double =
        delegate.moonAltitudeDegrees(time, observer)

    private fun buildEphemeris(localDate: LocalDate, observer: AstroObserver): CachedEphemeris {
        val start = localDate.atStartOfDay(observer.zoneId).toInstant()
        val end = localDate.plusDays(EPHEMERIS_DAYS)
            .atStartOfDay(observer.zoneId)
            .toInstant()
        return CachedEphemeris(
            start = start,
            end = end,
            events = delegate.eventsBetween(start, end, observer)
        )
    }

    private data class CacheKey(
        val observer: AstroObserver,
        val localDate: LocalDate
    )

    private data class CachedEphemeris(
        val start: Instant,
        val end: Instant,
        val events: List<AstroEvent>
    )

    private companion object {
        private const val MAX_CACHED_DATES = 2
        private const val EPHEMERIS_DAYS = 3L
    }
}
