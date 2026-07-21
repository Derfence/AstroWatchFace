package com.derfence.astroface.wear.complication

import com.derfence.astroface.wear.astro.AstroEvent
import com.derfence.astroface.wear.astro.AstroEventSource
import com.derfence.astroface.wear.astro.AstroObserver
import java.time.Duration
import java.time.Instant
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Dial24hAnnualAccuracyTest {
    private val observer = AstroObserver.DEFAULT

    @Test
    fun minuteProjectionStaysWithinOneEighthDegreeThroughout2026() {
        val yearStart = Instant.parse("2026-01-01T00:00:00Z")
        val yearEnd = Instant.parse("2027-01-01T00:00:00Z")
        var instant = yearStart

        while (instant.isBefore(yearEnd)) {
            listOf(0L, 29L, 30L, 59L).forEach { secondOffset ->
                val sample = instant.plusSeconds(secondOffset)
                val local = sample.atZone(observer.zoneId).toLocalTime()
                val exactAngle = (
                    local.toSecondOfDay() + local.nano / 1_000_000_000.0
                    ) / 240.0
                val minute = Dial24hProjectionBuilder
                    .civilMinuteBoundary(sample, observer)
                    .minuteOfDay
                val encodedAngle = minute * 0.25
                val rawDifference = abs(encodedAngle - exactAngle)
                val circularDifference = minOf(rawDifference, 360.0 - rawDifference)

                assertTrue(circularDifference <= 0.125)
            }
            instant = instant.plus(Duration.ofMinutes(10))
        }
    }

    @Test
    fun everyTenMinuteRequestIn2026ProducesANonEmptyContinuousTimeline() {
        val planner = Dial24hTimelinePlanner(EmptySource())
        val yearEnd = Instant.parse("2027-01-01T00:00:00Z")
        var instant = Instant.parse("2026-01-01T00:00:00Z")

        while (instant.isBefore(yearEnd)) {
            val entries = planner.plan(instant)

            assertFalse(entries.isEmpty())
            assertEquals(instant, entries.first().start)
            assertEquals(instant.plus(Duration.ofHours(10)), entries.last().end)
            entries.zipWithNext().forEach { (first, second) ->
                assertEquals(first.end, second.start)
            }
            instant = instant.plus(Duration.ofMinutes(10))
        }
    }

    private class EmptySource : AstroEventSource {
        override fun eventsBetween(
            start: Instant,
            end: Instant,
            observer: AstroObserver
        ): List<AstroEvent> = emptyList()

        override fun sunAltitudeDegrees(time: Instant, observer: AstroObserver): Double = 0.0

        override fun moonAltitudeDegrees(time: Instant, observer: AstroObserver): Double = -5.0
    }
}
