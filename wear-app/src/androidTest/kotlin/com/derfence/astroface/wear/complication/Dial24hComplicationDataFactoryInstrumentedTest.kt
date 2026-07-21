package com.derfence.astroface.wear.complication

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.wear.watchface.complications.data.ComplicationType
import com.derfence.astroface.wear.astro.AstroEvent
import com.derfence.astroface.wear.astro.AstroEventSource
import com.derfence.astroface.wear.astro.AstroEventType
import com.derfence.astroface.wear.astro.AstroObserver
import java.time.Duration
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Dial24hComplicationDataFactoryInstrumentedTest {
    @Test
    fun returnsAContiguousTenHourRangedTimelineAndReusesIdenticalPayloads() {
        val start = Instant.parse("2026-07-20T10:00:00Z")
        val source = FakeSource(
            listOf(
                AstroEvent(AstroEventType.MOONRISE, start.plus(Duration.ofHours(26))),
                AstroEvent(AstroEventType.MOONRISE, start.plus(Duration.ofHours(27)))
            )
        )
        val planner = Dial24hTimelinePlanner(source)

        val timeline = Dial24hComplicationDataFactory.createTimeline(start, planner)
        val entries = timeline.timelineEntries.toList()

        assertEquals(ComplicationType.RANGED_VALUE, timeline.defaultComplicationData.type)
        assertEquals(start, entries.first().validity.start)
        assertEquals(
            start.plus(Duration.ofHours(10)),
            entries.last().validity.end
        )
        entries.zipWithNext().forEach { (first, second) ->
            assertEquals(first.validity.end, second.validity.start)
        }
        assertSame(
            entries[1].complicationData,
            entries[2].complicationData
        )
        assertTrue(entries.isNotEmpty())
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
