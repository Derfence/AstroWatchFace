package com.derfence.astroface.wear.astro

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AstroWindowCalculatorTest {
    private val observer = AstroObserver.DEFAULT

    @Test
    fun windowUsesNextTwentyFourHoursFromRoundedCurrentInstant() {
        val now = Instant.parse("2026-07-07T10:15:40Z")
        val inside = AstroEvent(AstroEventType.SUNSET, Instant.parse("2026-07-08T08:00:00Z"))
        val source = FakeAstroEventSource(
            events = listOf(
                AstroEvent(AstroEventType.SUNRISE, Instant.parse("2026-07-07T10:14:00Z")),
                inside,
                AstroEvent(AstroEventType.MOONRISE, Instant.parse("2026-07-08T10:16:00Z"))
            ),
            sunAltitudeDegrees = 12.0
        )

        val window = AstroWindowCalculator(source).calculate(now, observer)

        assertEquals(Instant.parse("2026-07-07T10:15:00Z"), window.start)
        assertEquals(Instant.parse("2026-07-08T10:15:00Z"), window.end)
        assertEquals(listOf(inside), window.events)
    }

    @Test
    fun moonVisibleWithoutRiseOrSetSpansTheWholeWindow() {
        val window = AstroWindowCalculator(
            FakeAstroEventSource(moonAltitudeDegrees = 4.0)
        ).calculate(Instant.parse("2026-07-07T10:15:40Z"), observer)

        val moonIntervals = window.intervals.filter { it.type == AstroIntervalType.MOON_VISIBLE }

        assertEquals(listOf(AstroInterval(AstroIntervalType.MOON_VISIBLE, window.start, window.end)), moonIntervals)
    }

    @Test
    fun moonInvisibleWithoutRiseOrSetCreatesNoMoonInterval() {
        val window = AstroWindowCalculator(
            FakeAstroEventSource(moonAltitudeDegrees = -2.0)
        ).calculate(Instant.parse("2026-07-07T10:15:40Z"), observer)

        assertTrue(window.intervals.none { it.type == AstroIntervalType.MOON_VISIBLE })
    }

    @Test
    fun moonriseWithoutMoonsetCreatesIntervalToWindowEnd() {
        val moonrise = Instant.parse("2026-07-07T14:00:00Z")
        val window = AstroWindowCalculator(
            FakeAstroEventSource(
                events = listOf(AstroEvent(AstroEventType.MOONRISE, moonrise)),
                moonAltitudeDegrees = -2.0
            )
        ).calculate(Instant.parse("2026-07-07T10:15:40Z"), observer)

        val moonIntervals = window.intervals.filter { it.type == AstroIntervalType.MOON_VISIBLE }

        assertEquals(listOf(AstroInterval(AstroIntervalType.MOON_VISIBLE, moonrise, window.end)), moonIntervals)
    }

    @Test
    fun moonsetWithoutMoonriseClosesIntervalFromWindowStart() {
        val moonset = Instant.parse("2026-07-07T14:00:00Z")
        val window = AstroWindowCalculator(
            FakeAstroEventSource(
                events = listOf(AstroEvent(AstroEventType.MOONSET, moonset)),
                moonAltitudeDegrees = 5.0
            )
        ).calculate(Instant.parse("2026-07-07T10:15:40Z"), observer)

        val moonIntervals = window.intervals.filter { it.type == AstroIntervalType.MOON_VISIBLE }

        assertEquals(listOf(AstroInterval(AstroIntervalType.MOON_VISIBLE, window.start, moonset)), moonIntervals)
    }

    private class FakeAstroEventSource(
        private val events: List<AstroEvent> = emptyList(),
        private val sunAltitudeDegrees: Double = 12.0,
        private val moonAltitudeDegrees: Double = -2.0
    ) : AstroEventSource {
        override fun eventsBetween(
            start: Instant,
            end: Instant,
            observer: AstroObserver
        ): List<AstroEvent> = events

        override fun sunAltitudeDegrees(time: Instant, observer: AstroObserver): Double =
            sunAltitudeDegrees

        override fun moonAltitudeDegrees(time: Instant, observer: AstroObserver): Double =
            moonAltitudeDegrees
    }
}
