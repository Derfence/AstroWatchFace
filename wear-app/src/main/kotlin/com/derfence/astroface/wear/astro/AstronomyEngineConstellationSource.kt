package com.derfence.astroface.wear.astro

import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.Direction
import io.github.cosinekitty.astronomy.Refraction
import io.github.cosinekitty.astronomy.horizon
import io.github.cosinekitty.astronomy.searchRiseSet
import java.time.Instant
import java.time.LocalDate

class AstronomyEngineConstellationSource internal constructor(
    private val catalog: ConstellationCatalog,
    private val refreshPolicy: ConstellationRefreshPolicy
) : ConstellationSource {
    constructor() : this(
        catalog = DefaultConstellationCatalog.value,
        refreshPolicy = ConstellationRefreshPolicy(AstronomyEngineSunriseProvider)
    )

    private val cache = SynchronizedLruCache<CacheKey, ConstellationSnapshot>(MAX_CACHED_WINDOWS)

    @Synchronized
    override fun constellationsAt(
        time: Instant,
        observer: AstroObserver
    ): ConstellationSnapshot {
        cache.firstOrNull { key, _ ->
            key.observer == observer &&
                !time.isBefore(key.refreshInstant) &&
                time.isBefore(key.nextRefreshInstant)
        }?.let { return it.copy(calculatedAt = time) }

        val target = refreshPolicy.targetFor(time, observer)
        val key = CacheKey(
            observer = observer,
            refreshInstant = target.refreshInstant,
            targetMidnight = target.targetMidnight,
            nextRefreshInstant = target.nextRefreshInstant
        )
        return cache.getOrPut(key) {
            buildSnapshot(time, observer, target)
        }.copy(calculatedAt = time)
    }

    private fun buildSnapshot(
        time: Instant,
        observer: AstroObserver,
        target: ConstellationRefreshTarget
    ): ConstellationSnapshot {
        val astronomyTime = target.targetMidnight.toAstronomyTime()
        val astronomyObserver = observer.toAstronomyObserver()
        val points = catalog.stars.associate { star ->
            val horizontal = horizon(
                astronomyTime,
                astronomyObserver,
                star.raHours,
                star.decDegrees,
                Refraction.None
            )
            star.id to SkyPoint(
                azimuthDegrees = horizontal.azimuth.normalizedDegrees(),
                zenithDistanceDegrees = 90.0 - horizontal.altitude
            )
        }
        val lines = catalog.segments.mapNotNull { segment ->
            val from = points[segment.fromStarId] ?: return@mapNotNull null
            val to = points[segment.toStarId] ?: return@mapNotNull null
            if (from.isNearZenith() || to.isNearZenith()) {
                ConstellationLine(
                    constellationId = segment.constellationId,
                    from = from,
                    to = to
                )
            } else {
                null
            }
        }

        return ConstellationSnapshot(
            calculatedAt = time,
            refreshInstant = target.refreshInstant,
            targetMidnight = target.targetMidnight,
            nextRefreshInstant = target.nextRefreshInstant,
            lines = lines
        )
    }

    private fun SkyPoint.isNearZenith(): Boolean =
        zenithDistanceDegrees <= ZENITH_RADIUS_DEGREES &&
            !zenithDistanceDegrees.isNaN() &&
            !azimuthDegrees.isNaN()

    private fun Double.normalizedDegrees(): Double {
        val remainder = this % FULL_CIRCLE_DEGREES
        return if (remainder < 0.0) remainder + FULL_CIRCLE_DEGREES else remainder
    }

    private data class CacheKey(
        val observer: AstroObserver,
        val refreshInstant: Instant,
        val targetMidnight: Instant,
        val nextRefreshInstant: Instant
    )

    private companion object {
        private const val FULL_CIRCLE_DEGREES = 360.0
        private const val ZENITH_RADIUS_DEGREES = 100.0
        private const val MAX_CACHED_WINDOWS = 2
    }
}

object AstronomyEngineSunriseProvider : SunriseProvider {
    override fun sunriseOn(date: LocalDate, observer: AstroObserver): Instant? {
        val dayStart = date.atStartOfDay(observer.zoneId).toInstant()
        val sunrise = searchRiseSet(
            Body.Sun,
            observer.toAstronomyObserver(),
            Direction.Rise,
            dayStart.toAstronomyTime(),
            SEARCH_LIMIT_DAYS
        )?.toInstant()

        return sunrise?.takeIf { it.atZone(observer.zoneId).toLocalDate() == date }
    }

    private const val SEARCH_LIMIT_DAYS = 1.2
}
