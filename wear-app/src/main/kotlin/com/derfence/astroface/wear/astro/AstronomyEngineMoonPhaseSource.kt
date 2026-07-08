package com.derfence.astroface.wear.astro

import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.Direction
import io.github.cosinekitty.astronomy.illumination
import io.github.cosinekitty.astronomy.moonPhase
import io.github.cosinekitty.astronomy.searchRiseSet
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

class AstronomyEngineMoonPhaseSource internal constructor(
    private val observer: AstroObserver,
    private val moonRiseSetSource: MoonRiseSetSource,
    private val moonPhaseCalculator: MoonPhaseCalculator
) : MoonPhaseSource {
    constructor(
        observer: AstroObserver = AstroObserver.DEFAULT
    ) : this(
        observer = observer,
        moonRiseSetSource = AstronomyEngineMoonRiseSetSource(),
        moonPhaseCalculator = AstronomyEngineMoonPhaseCalculator()
    )

    override fun phaseAt(time: Instant): MoonPhaseSnapshot {
        val updateBoundary = moonRiseSetSource.previousMoonsetAtOrBefore(time, observer)
        val targetSearchStart = (updateBoundary ?: time).plus(SEARCH_STEP)
        val targetTime = moonRiseSetSource.nextMoonriseAfter(targetSearchStart, observer) ?: time
        val values = moonPhaseCalculator.phaseAt(targetTime)

        return MoonPhaseSnapshot(
            calculatedAt = time,
            targetTime = targetTime,
            phaseAngleDegrees = values.phaseAngleDegrees,
            illuminationPercent = values.illuminationPercent,
            validUntil = moonRiseSetSource.nextMoonsetAfter(time.plus(SEARCH_STEP), observer)
        )
    }

    private companion object {
        private val SEARCH_STEP = Duration.ofMinutes(1)
    }
}

internal interface MoonRiseSetSource {
    fun previousMoonsetAtOrBefore(time: Instant, observer: AstroObserver): Instant?
    fun nextMoonsetAfter(time: Instant, observer: AstroObserver): Instant?
    fun nextMoonriseAfter(time: Instant, observer: AstroObserver): Instant?
}

internal interface MoonPhaseCalculator {
    fun phaseAt(time: Instant): MoonPhaseValues
}

internal data class MoonPhaseValues(
    val phaseAngleDegrees: Double,
    val illuminationPercent: Int
)

private class AstronomyEngineMoonRiseSetSource : MoonRiseSetSource {
    override fun previousMoonsetAtOrBefore(time: Instant, observer: AstroObserver): Instant? =
        moonEvents(
            direction = Direction.Set,
            start = time.minus(PAST_SEARCH_WINDOW),
            end = time.plus(INCLUSION_MARGIN),
            observer = observer
        ).lastOrNull { !it.isAfter(time) }

    override fun nextMoonsetAfter(time: Instant, observer: AstroObserver): Instant? =
        moonEvents(
            direction = Direction.Set,
            start = time,
            end = time.plus(FUTURE_SEARCH_WINDOW),
            observer = observer
        ).firstOrNull { it.isAfter(time) }

    override fun nextMoonriseAfter(time: Instant, observer: AstroObserver): Instant? =
        moonEvents(
            direction = Direction.Rise,
            start = time,
            end = time.plus(FUTURE_SEARCH_WINDOW),
            observer = observer
        ).firstOrNull { it.isAfter(time) }

    private fun moonEvents(
        direction: Direction,
        start: Instant,
        end: Instant,
        observer: AstroObserver
    ): List<Instant> {
        val events = mutableListOf<Instant>()
        val astronomyObserver = observer.toAstronomyObserver()
        var cursor = start
        var attempts = 0

        while (!cursor.isAfter(end) && attempts < MAX_SEARCH_ATTEMPTS) {
            val limitDays = Duration.between(cursor, end).toMillis() / MILLIS_PER_DAY + SEARCH_MARGIN_DAYS
            val result = searchRiseSet(
                Body.Moon,
                astronomyObserver,
                direction,
                cursor.toAstronomyTime(),
                limitDays
            ) ?: break
            val instant = result.toInstant().truncatedTo(ChronoUnit.MINUTES)

            if (instant.isAfter(end)) {
                break
            }
            if (!instant.isBefore(start)) {
                events += instant
            }

            val nextCursor = instant.plus(SEARCH_STEP)
            cursor = if (nextCursor.isAfter(cursor)) nextCursor else cursor.plus(SEARCH_STEP)
            attempts += 1
        }

        return events.distinct()
    }

    private companion object {
        private const val MILLIS_PER_DAY = 86_400_000.0
        private const val SEARCH_MARGIN_DAYS = 0.08
        private const val MAX_SEARCH_ATTEMPTS = 16
        private val SEARCH_STEP = Duration.ofMinutes(1)
        private val INCLUSION_MARGIN = Duration.ofSeconds(1)
        private val PAST_SEARCH_WINDOW = Duration.ofDays(4)
        private val FUTURE_SEARCH_WINDOW = Duration.ofDays(8)
    }
}

private class AstronomyEngineMoonPhaseCalculator : MoonPhaseCalculator {
    override fun phaseAt(time: Instant): MoonPhaseValues {
        val astronomyTime = time.toAstronomyTime()
        val phaseAngleDegrees = moonPhase(astronomyTime).normalizedDegrees()
        val illuminationPercent = (illumination(Body.Moon, astronomyTime).phaseFraction * 100.0)
            .roundToInt()
            .coerceIn(MIN_PERCENT, MAX_PERCENT)

        return MoonPhaseValues(
            phaseAngleDegrees = phaseAngleDegrees,
            illuminationPercent = illuminationPercent
        )
    }

    private companion object {
        private const val MIN_PERCENT = 0
        private const val MAX_PERCENT = 100
    }
}
