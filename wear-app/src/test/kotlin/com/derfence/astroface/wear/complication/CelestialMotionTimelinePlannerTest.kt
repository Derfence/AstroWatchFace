package com.derfence.astroface.wear.complication

import com.derfence.astroface.wear.astro.AstroObserver
import com.derfence.astroface.wear.astro.CelestialBody
import com.derfence.astroface.wear.astro.CelestialPosition
import com.derfence.astroface.wear.astro.CelestialPositionSnapshot
import com.derfence.astroface.wear.astro.CelestialPositionSource
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CelestialMotionTimelinePlannerTest {
    private val zoneId = ZoneId.of("Europe/Paris")

    @Test
    fun alignedRequestProducesTwelveEntriesAndThirteenCalculations() {
        val source = CountingSource()
        val planner = planner(source)
        val start = Instant.parse("2026-07-20T10:00:00Z")

        val entries = planner.plan(start, CelestialMotionGroup.INNER)

        assertEquals(12, entries.size)
        assertEquals(13, source.calls)
        assertEquals(start, entries.first().start)
        assertEquals(start.plus(Duration.ofHours(2)), entries.last().end)
        assertContiguous(entries)
    }

    @Test
    fun unalignedRequestProducesThirteenEntriesAndFourteenCalculations() {
        val source = CountingSource()
        val planner = planner(source)
        val start = Instant.parse("2026-07-20T10:07:25Z")

        val entries = planner.plan(start, CelestialMotionGroup.MIDDLE)

        assertEquals(13, entries.size)
        assertEquals(14, source.calls)
        assertEquals(start, entries.first().start)
        assertEquals(start.plus(Duration.ofHours(2)), entries.last().end)
        assertContiguous(entries)
    }

    @Test
    fun alignsAcrossDaylightSavingTimeChanges() {
        val planner = planner(CountingSource())
        val beforeSpringJump = Instant.parse("2026-03-29T00:57:00Z")
        val beforeAutumnOverlap = Instant.parse("2026-10-25T00:57:00Z")

        assertEquals(
            Instant.parse("2026-03-29T00:50:00Z"),
            planner.alignedBoundaryAtOrBefore(beforeSpringJump)
        )
        assertEquals(
            Instant.parse("2026-10-25T00:50:00Z"),
            planner.alignedBoundaryAtOrBefore(beforeAutumnOverlap)
        )
        assertContiguous(planner.plan(beforeSpringJump, CelestialMotionGroup.OUTER))
        assertContiguous(planner.plan(beforeAutumnOverlap, CelestialMotionGroup.OUTER))
    }

    @Test
    fun changingZoneChangesTheLocalAlignment() {
        val instant = Instant.parse("2026-07-20T10:07:00Z")
        val parisBoundary = planner(CountingSource(), zoneId)
            .alignedBoundaryAtOrBefore(instant)
        val kathmanduBoundary = planner(CountingSource(), ZoneId.of("Asia/Kathmandu"))
            .alignedBoundaryAtOrBefore(instant)

        assertEquals(Instant.parse("2026-07-20T10:00:00Z"), parisBoundary)
        assertEquals(Instant.parse("2026-07-20T10:05:00Z"), kathmanduBoundary)
    }

    private fun planner(
        source: CelestialPositionSource,
        zone: ZoneId = zoneId
    ) = CelestialMotionTimelinePlanner(source, AstroObserver.DEFAULT, zone)

    private fun assertContiguous(entries: List<CelestialMotionTimelineEntry>) {
        entries.zipWithNext().forEach { (first, second) ->
            assertEquals(first.end, second.start)
        }
        assertTrue(entries.all { it.start.isBefore(it.end) })
    }

    private class CountingSource : CelestialPositionSource {
        var calls = 0

        override fun positionsAt(
            time: Instant,
            observer: AstroObserver
        ): CelestialPositionSnapshot {
            calls += 1
            return CelestialPositionSnapshot(
                calculatedAt = time,
                positions = CelestialBody.entries.mapIndexed { index, body ->
                    CelestialPosition(body, (index * 30.0 + calls) % 360.0)
                }
            )
        }
    }
}
