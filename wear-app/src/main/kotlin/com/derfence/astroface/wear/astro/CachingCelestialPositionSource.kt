package com.derfence.astroface.wear.astro

import java.time.Instant

class CachingCelestialPositionSource(
    private val delegate: CelestialPositionSource = AstronomyEngineCelestialPositionSource(),
    maxEntries: Int = DEFAULT_MAX_ENTRIES
) : CelestialPositionSource {
    private val cache = SynchronizedLruCache<CacheKey, CelestialPositionSnapshot>(maxEntries)

    override fun positionsAt(
        time: Instant,
        observer: AstroObserver
    ): CelestialPositionSnapshot =
        cache.getOrPut(CacheKey(observer, time)) {
            delegate.positionsAt(time, observer)
        }

    private data class CacheKey(
        val observer: AstroObserver,
        val time: Instant
    )

    companion object {
        internal const val DEFAULT_MAX_ENTRIES = 128
    }
}
