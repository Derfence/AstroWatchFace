package com.derfence.astroface.wear.astro

import io.github.cosinekitty.astronomy.Aberration
import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.Direction
import io.github.cosinekitty.astronomy.EquatorEpoch
import io.github.cosinekitty.astronomy.Refraction
import io.github.cosinekitty.astronomy.Time
import io.github.cosinekitty.astronomy.equator
import io.github.cosinekitty.astronomy.horizon
import io.github.cosinekitty.astronomy.searchAltitude
import io.github.cosinekitty.astronomy.searchRiseSet
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

class AstronomyEngineAstroEventSource : AstroEventSource {
    override fun eventsBetween(
        start: Instant,
        end: Instant,
        observer: AstroObserver
    ): List<AstroEvent> {
        val astroObserver = observer.toAstronomyObserver()
        return buildList {
            addAll(searchEvents(start, end, AstroEventType.SUNRISE) { cursor, limitDays ->
                searchRiseSet(Body.Sun, astroObserver, Direction.Rise, cursor, limitDays)
            })
            addAll(searchEvents(start, end, AstroEventType.SUNSET) { cursor, limitDays ->
                searchRiseSet(Body.Sun, astroObserver, Direction.Set, cursor, limitDays)
            })
            addAll(searchEvents(start, end, AstroEventType.ASTRONOMICAL_DAWN) { cursor, limitDays ->
                searchAltitude(
                    Body.Sun,
                    astroObserver,
                    Direction.Rise,
                    cursor,
                    limitDays,
                    AstroWindowCalculator.ASTRONOMICAL_ALTITUDE_DEGREES
                )
            })
            addAll(searchEvents(start, end, AstroEventType.ASTRONOMICAL_DUSK) { cursor, limitDays ->
                searchAltitude(
                    Body.Sun,
                    astroObserver,
                    Direction.Set,
                    cursor,
                    limitDays,
                    AstroWindowCalculator.ASTRONOMICAL_ALTITUDE_DEGREES
                )
            })
            addAll(searchEvents(start, end, AstroEventType.MOONRISE) { cursor, limitDays ->
                searchRiseSet(Body.Moon, astroObserver, Direction.Rise, cursor, limitDays)
            })
            addAll(searchEvents(start, end, AstroEventType.MOONSET) { cursor, limitDays ->
                searchRiseSet(Body.Moon, astroObserver, Direction.Set, cursor, limitDays)
            })
        }.sortedBy { it.time }
    }

    override fun sunAltitudeDegrees(time: Instant, observer: AstroObserver): Double =
        altitudeDegrees(Body.Sun, time, observer, Refraction.None)

    override fun moonAltitudeDegrees(time: Instant, observer: AstroObserver): Double =
        altitudeDegrees(Body.Moon, time, observer, Refraction.Normal)

    private fun searchEvents(
        start: Instant,
        end: Instant,
        type: AstroEventType,
        search: (Time, Double) -> Time?
    ): List<AstroEvent> {
        val events = mutableListOf<AstroEvent>()
        var cursor = start
        var attempts = 0

        while (!cursor.isAfter(end) && attempts < MAX_SEARCH_ATTEMPTS) {
            val limitDays = Duration.between(cursor, end).toMillis() / MILLIS_PER_DAY + SEARCH_MARGIN_DAYS
            val result = search(cursor.toAstronomyTime(), limitDays) ?: break
            val instant = result.toInstant().truncatedTo(ChronoUnit.MINUTES)

            if (instant.isAfter(end)) {
                break
            }
            if (!instant.isBefore(start)) {
                events += AstroEvent(type, instant)
            }

            val nextCursor = instant.plus(SEARCH_STEP)
            cursor = if (nextCursor.isAfter(cursor)) nextCursor else cursor.plus(SEARCH_STEP)
            attempts += 1
        }

        return events.distinctBy { it.type to it.time }
    }

    private fun altitudeDegrees(
        body: Body,
        instant: Instant,
        observer: AstroObserver,
        refraction: Refraction
    ): Double {
        val time = instant.toAstronomyTime()
        val astronomyObserver = observer.toAstronomyObserver()
        val equatorial = equator(
            body,
            time,
            astronomyObserver,
            EquatorEpoch.OfDate,
            Aberration.Corrected
        )
        return horizon(
            time,
            astronomyObserver,
            equatorial.ra,
            equatorial.dec,
            refraction
        ).altitude
    }

    companion object {
        private const val MILLIS_PER_DAY = 86_400_000.0
        private const val SEARCH_MARGIN_DAYS = 0.08
        private const val MAX_SEARCH_ATTEMPTS = 8
        private val SEARCH_STEP = Duration.ofMinutes(1)
    }
}
