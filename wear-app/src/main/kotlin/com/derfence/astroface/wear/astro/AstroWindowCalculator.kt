package com.derfence.astroface.wear.astro

import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

class AstroWindowCalculator(
    private val source: AstroEventSource = AstronomyEngineAstroEventSource()
) {
    fun calculate(
        now: Instant,
        observer: AstroObserver = AstroObserver.DEFAULT
    ): RollingAstroWindow {
        val start = now.truncatedTo(ChronoUnit.MINUTES)
        val end = start.plus(WINDOW_DURATION)
        val events = source.eventsBetween(start, end, observer)
            .filter { !it.time.isBefore(start) && !it.time.isAfter(end) }
            .sortedBy { it.time }

        return RollingAstroWindow(
            start = start,
            end = end,
            events = events,
            intervals = buildIntervals(start, end, events, observer)
        )
    }

    private fun buildIntervals(
        start: Instant,
        end: Instant,
        events: List<AstroEvent>,
        observer: AstroObserver
    ): List<AstroInterval> =
        buildList {
            addAll(buildIntervalsFor(
                type = AstroIntervalType.SUNLIGHT,
                isActiveAtStart = source.sunAltitudeDegrees(start, observer) > SUNRISE_ALTITUDE_DEGREES,
                events = events,
                startEvents = setOf(AstroEventType.SUNRISE),
                endEvents = setOf(AstroEventType.SUNSET),
                windowStart = start,
                windowEnd = end
            ))
            addAll(buildIntervalsFor(
                type = AstroIntervalType.ASTRONOMICAL_TWILIGHT,
                isActiveAtStart = source.sunAltitudeDegrees(start, observer)
                    .let { it > ASTRONOMICAL_ALTITUDE_DEGREES && it <= SUNRISE_ALTITUDE_DEGREES },
                events = events,
                startEvents = setOf(AstroEventType.SUNSET, AstroEventType.ASTRONOMICAL_DAWN),
                endEvents = setOf(AstroEventType.ASTRONOMICAL_DUSK, AstroEventType.SUNRISE),
                windowStart = start,
                windowEnd = end
            ))
            addAll(buildIntervalsFor(
                type = AstroIntervalType.ASTRONOMICAL_NIGHT,
                isActiveAtStart = source.sunAltitudeDegrees(start, observer) <= ASTRONOMICAL_ALTITUDE_DEGREES,
                events = events,
                startEvents = setOf(AstroEventType.ASTRONOMICAL_DUSK),
                endEvents = setOf(AstroEventType.ASTRONOMICAL_DAWN),
                windowStart = start,
                windowEnd = end
            ))
            addAll(buildIntervalsFor(
                type = AstroIntervalType.MOON_VISIBLE,
                isActiveAtStart = source.moonAltitudeDegrees(start, observer) > HORIZON_ALTITUDE_DEGREES,
                events = events,
                startEvents = setOf(AstroEventType.MOONRISE),
                endEvents = setOf(AstroEventType.MOONSET),
                windowStart = start,
                windowEnd = end
            ))
        }.sortedWith(compareBy<AstroInterval> { it.start }.thenBy { it.type.name })

    private fun buildIntervalsFor(
        type: AstroIntervalType,
        isActiveAtStart: Boolean,
        events: List<AstroEvent>,
        startEvents: Set<AstroEventType>,
        endEvents: Set<AstroEventType>,
        windowStart: Instant,
        windowEnd: Instant
    ): List<AstroInterval> {
        val intervals = mutableListOf<AstroInterval>()
        var activeStart: Instant? = if (isActiveAtStart) windowStart else null

        events.forEach { event ->
            when (event.type) {
                in startEvents -> if (activeStart == null) {
                    activeStart = event.time
                }
                in endEvents -> {
                    val start = activeStart
                    if (start != null && event.time.isAfter(start)) {
                        intervals += AstroInterval(type, start, event.time)
                    }
                    activeStart = null
                }
                else -> Unit
            }
        }

        val start = activeStart
        if (start != null && windowEnd.isAfter(start)) {
            intervals += AstroInterval(type, start, windowEnd)
        }

        return intervals
    }

    companion object {
        private val WINDOW_DURATION: Duration = Duration.ofHours(24)
        const val SUNRISE_ALTITUDE_DEGREES = -0.833
        const val ASTRONOMICAL_ALTITUDE_DEGREES = -18.0
        private const val HORIZON_ALTITUDE_DEGREES = 0.0
    }
}
