package com.derfence.astroface.wear.astro

import io.github.cosinekitty.astronomy.Aberration
import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.Direction
import io.github.cosinekitty.astronomy.EquatorEpoch
import io.github.cosinekitty.astronomy.Observer
import io.github.cosinekitty.astronomy.Refraction
import io.github.cosinekitty.astronomy.Time
import io.github.cosinekitty.astronomy.equator
import io.github.cosinekitty.astronomy.horizon
import io.github.cosinekitty.astronomy.searchRiseSet
import java.time.Duration
import java.time.Instant
import java.time.LocalDate

class AstronomyEngineCelestialHorizonSource : CelestialHorizonSource {
    private var cachedKey: CacheKey? = null
    private var cachedSnapshot: CelestialHorizonSnapshot? = null

    override fun horizonMarkersAt(
        time: Instant,
        observer: AstroObserver
    ): CelestialHorizonSnapshot {
        val localDate = time.atZone(observer.zoneId).toLocalDate()
        val cached = cachedSnapshot
        if (cached != null && cachedKey == CacheKey(observer, localDate)) {
            return cached.copy(calculatedAt = time)
        }

        val snapshot = buildSnapshot(time, observer, localDate)
        cachedKey = CacheKey(observer, localDate)
        cachedSnapshot = snapshot
        return snapshot
    }

    private fun buildSnapshot(
        time: Instant,
        observer: AstroObserver,
        localDate: LocalDate
    ): CelestialHorizonSnapshot {
        val dayStart = localDate.atStartOfDay(observer.zoneId).toInstant()
        val dayEnd = localDate.plusDays(1).atStartOfDay(observer.zoneId).toInstant()
        val astronomyObserver = observer.toAstronomyObserver()
        val markers = CelestialBody.entries.flatMap { body ->
            listOfNotNull(
                searchMarker(
                    body = body,
                    type = CelestialHorizonEventType.RISE,
                    direction = Direction.Rise,
                    dayStart = dayStart,
                    dayEnd = dayEnd,
                    observer = astronomyObserver
                ),
                searchMarker(
                    body = body,
                    type = CelestialHorizonEventType.SET,
                    direction = Direction.Set,
                    dayStart = dayStart,
                    dayEnd = dayEnd,
                    observer = astronomyObserver
                )
            )
        }

        return CelestialHorizonSnapshot(
            calculatedAt = time,
            localDate = localDate,
            markers = markers
        )
    }

    private fun searchMarker(
        body: CelestialBody,
        type: CelestialHorizonEventType,
        direction: Direction,
        dayStart: Instant,
        dayEnd: Instant,
        observer: Observer
    ): CelestialHorizonMarker? {
        val astronomyBody = body.toAstronomyBody()
        val result = searchRiseSet(
            astronomyBody,
            observer,
            direction,
            dayStart.toAstronomyTime(),
            searchLimitDays(dayStart, dayEnd)
        ) ?: return null
        val instant = result.toInstant()

        if (instant.isBefore(dayStart) || !instant.isBefore(dayEnd)) {
            return null
        }

        return CelestialHorizonMarker(
            body = body,
            type = type,
            time = instant,
            azimuthDegrees = azimuthDegrees(astronomyBody, result, observer)
        )
    }

    private fun azimuthDegrees(
        body: Body,
        time: Time,
        observer: Observer
    ): Double {
        val equatorial = equator(
            body,
            time,
            observer,
            EquatorEpoch.OfDate,
            Aberration.Corrected
        )
        return horizon(
            time,
            observer,
            equatorial.ra,
            equatorial.dec,
            Refraction.None
        ).azimuth.normalizedDegrees()
    }

    private fun searchLimitDays(start: Instant, end: Instant): Double =
        Duration.between(start, end).toMillis() / MILLIS_PER_DAY + SEARCH_MARGIN_DAYS

    private data class CacheKey(
        val observer: AstroObserver,
        val localDate: LocalDate
    )

    private companion object {
        private const val MILLIS_PER_DAY = 86_400_000.0
        private const val SEARCH_MARGIN_DAYS = 0.08
    }
}
