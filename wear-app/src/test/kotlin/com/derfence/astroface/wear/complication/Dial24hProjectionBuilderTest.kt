package com.derfence.astroface.wear.complication

import com.derfence.astroface.wear.astro.AstroEvent
import com.derfence.astroface.wear.astro.AstroEventSource
import com.derfence.astroface.wear.astro.AstroEventType
import com.derfence.astroface.wear.astro.AstroObserver
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class Dial24hProjectionBuilderTest {
    private val observer = AstroObserver.DEFAULT
    private val instant = Instant.parse("2026-07-20T10:00:00Z")

    @Test
    fun projectsSixRoundedCivilBoundaries() {
        val source = FakeSource(
            events = listOf(
                event(AstroEventType.ASTRONOMICAL_DAWN, "2026-07-21T02:12:29Z"),
                event(AstroEventType.SUNRISE, "2026-07-21T04:12:30Z"),
                event(AstroEventType.SUNSET, "2026-07-20T19:30:00Z"),
                event(AstroEventType.ASTRONOMICAL_DUSK, "2026-07-20T22:00:00Z"),
                event(AstroEventType.MOONRISE, "2026-07-20T18:00:00Z"),
                event(AstroEventType.MOONSET, "2026-07-21T01:00:00Z")
            )
        )

        val projection = Dial24hProjectionBuilder(source).projectionAt(instant, observer)

        assertEquals(Dial24hBoundary.Minute(252), projection.astronomicalDawn)
        assertEquals(Dial24hBoundary.Minute(373), projection.sunrise)
        assertEquals(Dial24hBoundary.Minute(1290), projection.sunset)
        assertEquals(Dial24hBoundary.Minute(0), projection.astronomicalDusk)
        assertEquals(Dial24hBoundary.Minute(1200), projection.moonVisibleStart)
        assertEquals(Dial24hBoundary.Minute(180), projection.moonVisibleEnd)
    }

    @Test
    fun usesNowForOneSidedMoonVisibility() {
        val riseOnly = Dial24hProjectionBuilder(
            FakeSource(listOf(event(AstroEventType.MOONRISE, "2026-07-20T18:00:00Z")))
        ).projectionAt(instant, observer)
        val setOnly = Dial24hProjectionBuilder(
            FakeSource(
                events = listOf(event(AstroEventType.MOONSET, "2026-07-20T18:00:00Z")),
                moonAltitude = 5.0
            )
        ).projectionAt(instant, observer)

        assertEquals(Dial24hBoundary.Now, riseOnly.moonVisibleEnd)
        assertEquals(Dial24hBoundary.Now, setOnly.moonVisibleStart)
    }

    @Test
    fun distinguishesFullDayFromAbsentMoonVisibility() {
        val fullDay = Dial24hProjectionBuilder(FakeSource(moonAltitude = 5.0))
            .projectionAt(instant, observer)
        val absent = Dial24hProjectionBuilder(FakeSource(moonAltitude = -5.0))
            .projectionAt(instant, observer)

        assertEquals(Dial24hBoundary.FullDay, fullDay.moonVisibleStart)
        assertEquals(Dial24hBoundary.FullDay, fullDay.moonVisibleEnd)
        assertEquals(Dial24hBoundary.Absent, absent.moonVisibleStart)
        assertEquals(Dial24hBoundary.Absent, absent.moonVisibleEnd)
    }

    private fun event(type: AstroEventType, time: String) = AstroEvent(type, Instant.parse(time))

    private class FakeSource(
        private val events: List<AstroEvent> = emptyList(),
        private val moonAltitude: Double = -5.0
    ) : AstroEventSource {
        override fun eventsBetween(
            start: Instant,
            end: Instant,
            observer: AstroObserver
        ): List<AstroEvent> = events.filter { !it.time.isBefore(start) && !it.time.isAfter(end) }

        override fun sunAltitudeDegrees(time: Instant, observer: AstroObserver): Double = 0.0

        override fun moonAltitudeDegrees(time: Instant, observer: AstroObserver): Double = moonAltitude
    }
}
