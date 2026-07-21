package com.derfence.astroface.wear.complication

import com.derfence.astroface.wear.astro.AstroEvent
import com.derfence.astroface.wear.astro.AstroEventSource
import com.derfence.astroface.wear.astro.AstroEventType
import com.derfence.astroface.wear.astro.AstroIntervalType
import com.derfence.astroface.wear.astro.AstroObserver
import com.derfence.astroface.wear.astro.AstroWindowCalculator
import com.derfence.astroface.wear.astro.SharedAstronomySources
import java.time.Duration
import java.time.Instant

class Dial24hProjectionBuilder(
    private val eventSource: AstroEventSource = SharedAstronomySources.astroEventSource,
    private val windowCalculator: AstroWindowCalculator = AstroWindowCalculator(eventSource)
) {
    fun projectionAt(
        instant: Instant,
        observer: AstroObserver = AstroObserver.DEFAULT
    ): Dial24hProjection {
        val window = windowCalculator.calculate(instant, observer)
        val solarEvents = eventSource.eventsBetween(
            window.start,
            window.end.plus(SOLAR_JOIN_TOLERANCE),
            observer
        )
        val moonEvents = window.events.filter { it.time.isAfter(window.start) }
        val moonrise = moonEvents.firstAfter(AstroEventType.MOONRISE)
        val moonset = moonEvents.firstAfter(AstroEventType.MOONSET)
        val moonVisibleAtStart = window.intervals.any { interval ->
            interval.type == AstroIntervalType.MOON_VISIBLE && interval.start == window.start
        }
        val moonBoundaries = moonBoundaries(moonrise, moonset, moonVisibleAtStart, observer)

        return Dial24hProjection(
            astronomicalDawn = solarEvents.boundaryAfter(
                AstroEventType.ASTRONOMICAL_DAWN,
                window.start,
                observer
            ),
            sunrise = solarEvents.boundaryAfter(AstroEventType.SUNRISE, window.start, observer),
            sunset = solarEvents.boundaryAfter(AstroEventType.SUNSET, window.start, observer),
            astronomicalDusk = solarEvents.boundaryAfter(
                AstroEventType.ASTRONOMICAL_DUSK,
                window.start,
                observer
            ),
            moonVisibleStart = moonBoundaries.first,
            moonVisibleEnd = moonBoundaries.second
        )
    }

    private fun moonBoundaries(
        moonrise: AstroEvent?,
        moonset: AstroEvent?,
        visibleAtStart: Boolean,
        observer: AstroObserver
    ): Pair<Dial24hBoundary, Dial24hBoundary> =
        when {
            moonrise != null && moonset != null ->
                civilMinuteBoundary(moonrise.time, observer) to
                    civilMinuteBoundary(moonset.time, observer)
            moonrise != null ->
                civilMinuteBoundary(moonrise.time, observer) to Dial24hBoundary.Now
            moonset != null ->
                Dial24hBoundary.Now to civilMinuteBoundary(moonset.time, observer)
            visibleAtStart -> Dial24hBoundary.FullDay to Dial24hBoundary.FullDay
            else -> Dial24hBoundary.Absent to Dial24hBoundary.Absent
        }

    private fun List<AstroEvent>.boundaryAfter(
        type: AstroEventType,
        start: Instant,
        observer: AstroObserver
    ): Dial24hBoundary =
        asSequence()
            .filter { it.type == type && it.time.isAfter(start) }
            .minByOrNull { it.time }
            ?.let { civilMinuteBoundary(it.time, observer) }
            ?: Dial24hBoundary.Absent

    private fun List<AstroEvent>.firstAfter(type: AstroEventType): AstroEvent? =
        asSequence()
            .filter { it.type == type }
            .minByOrNull { it.time }

    companion object {
        val SOLAR_JOIN_TOLERANCE: Duration = Duration.ofMinutes(10)

        internal fun civilMinuteBoundary(
            time: Instant,
            observer: AstroObserver
        ): Dial24hBoundary.Minute {
            val local = time.atZone(observer.zoneId)
            val secondsOfDay = local.hour * SECONDS_PER_HOUR +
                local.minute * SECONDS_PER_MINUTE + local.second
            val roundedMinute = ((secondsOfDay + ROUNDING_SECONDS) / SECONDS_PER_MINUTE) %
                MINUTES_PER_DAY
            return Dial24hBoundary.Minute(roundedMinute)
        }

        private const val ROUNDING_SECONDS = 30
        private const val SECONDS_PER_MINUTE = 60
        private const val SECONDS_PER_HOUR = 60 * SECONDS_PER_MINUTE
        private const val MINUTES_PER_DAY = 24 * 60
    }
}
