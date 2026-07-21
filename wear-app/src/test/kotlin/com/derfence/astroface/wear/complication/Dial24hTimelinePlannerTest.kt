package com.derfence.astroface.wear.complication

import com.derfence.astroface.wear.astro.AstroEvent
import com.derfence.astroface.wear.astro.AstroEventSource
import com.derfence.astroface.wear.astro.AstroEventType
import com.derfence.astroface.wear.astro.AstroObserver
import java.time.Duration
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Dial24hTimelinePlannerTest {
    @Test
    fun coversTenHoursWithContiguousEventDrivenEntries() {
        val request = Instant.parse("2026-07-20T10:00:00Z")
        val source = FakeSource(
            listOf(
                AstroEvent(AstroEventType.SUNSET, Instant.parse("2026-07-20T12:03:20Z")),
                AstroEvent(AstroEventType.MOONRISE, Instant.parse("2026-07-21T12:05:10Z"))
            )
        )

        val entries = Dial24hTimelinePlanner(source).plan(request)

        assertEquals(request, entries.first().start)
        assertEquals(request.plus(Duration.ofHours(10)), entries.last().end)
        assertTrue(entries.any { it.start == Instant.parse("2026-07-20T12:04:00Z") })
        assertTrue(entries.any { it.start == Instant.parse("2026-07-20T12:06:00Z") })
        entries.zipWithNext().forEach { (first, second) -> assertEquals(first.end, second.start) }
        assertTrue(entries.all { it.start.isBefore(it.end) })
    }

    @Test
    fun remainsContinuousAcrossParisClockChanges() {
        listOf(
            Instant.parse("2026-03-29T00:30:00Z"),
            Instant.parse("2026-10-25T00:30:00Z")
        ).forEach { request ->
            val entries = Dial24hTimelinePlanner(FakeSource(emptyList())).plan(request)

            assertEquals(request, entries.first().start)
            assertEquals(request.plus(Duration.ofHours(10)), entries.last().end)
            entries.zipWithNext().forEach { (first, second) -> assertEquals(first.end, second.start) }
        }
    }

    private class FakeSource(private val events: List<AstroEvent>) : AstroEventSource {
        override fun eventsBetween(
            start: Instant,
            end: Instant,
            observer: AstroObserver
        ): List<AstroEvent> = events.filter { !it.time.isBefore(start) && !it.time.isAfter(end) }

        override fun sunAltitudeDegrees(time: Instant, observer: AstroObserver): Double = 0.0

        override fun moonAltitudeDegrees(time: Instant, observer: AstroObserver): Double = -5.0
    }
}
