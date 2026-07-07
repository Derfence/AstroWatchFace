package com.derfence.astroface.wear.dial

import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DialGeometryTest {
    @Test
    fun twentyFourHourDialHasTwentyFourTicks() {
        assertEquals(24, DialGeometry.twentyFourHourTicks().size)
    }

    @Test
    fun twentyFourHourDialLabelsEveryThreeHours() {
        val labels = DialGeometry.twentyFourHourTicks().mapNotNull { it.label }

        assertEquals(listOf("00", "03", "06", "09", "12", "15", "18", "21"), labels)
    }

    @Test
    fun midnightIsAtTop() {
        val midnight = DialGeometry.twentyFourHourTicks().first()
        val point = DialGeometry.point(radius = 100f, angleDegrees = midnight.angleDegrees)

        assertEquals(0f, midnight.angleDegrees, 0.001f)
        assertEquals(DialGeometry.center, point.x, 0.001f)
        assertEquals(DialGeometry.center - 100f, point.y, 0.001f)
    }

    @Test
    fun twelveHourDialHasTwelveIndexesWithoutLabels() {
        val ticks = DialGeometry.twelveHourTicks()

        assertEquals(12, ticks.size)
        ticks.forEach { assertNull(it.label) }
    }

    @Test
    fun localCivilTimeMapsToTwentyFourHourAngle() {
        assertEquals(0f, DialGeometry.angleForTime(LocalTime.MIDNIGHT), 0.001f)
        assertEquals(90f, DialGeometry.angleForTime(LocalTime.of(6, 0)), 0.001f)
        assertEquals(180f, DialGeometry.angleForTime(LocalTime.NOON), 0.001f)
        assertEquals(270f, DialGeometry.angleForTime(LocalTime.of(18, 0)), 0.001f)
        assertEquals(359.75f, DialGeometry.angleForTime(LocalTime.of(23, 59)), 0.001f)
    }

    @Test
    fun angleUsesCivilTimeAcrossDaylightSavingOverlap() {
        val zoneId = ZoneId.of("Europe/Paris")
        val repeatedTime = LocalDateTime.of(2026, 10, 25, 2, 30)
        val earlierOffset = ZonedDateTime.ofLocal(repeatedTime, zoneId, ZoneOffset.ofHours(2))
        val laterOffset = ZonedDateTime.ofLocal(repeatedTime, zoneId, ZoneOffset.ofHours(1))

        assertEquals(
            DialGeometry.angleForInstant(earlierOffset.toInstant(), zoneId),
            DialGeometry.angleForInstant(laterOffset.toInstant(), zoneId),
            0.001f
        )
    }

    @Test
    fun arcSegmentsSplitAtMidnight() {
        val zoneId = ZoneId.of("Europe/Paris")
        val start = ZonedDateTime.of(2026, 7, 7, 23, 0, 0, 0, zoneId).toInstant()
        val end = ZonedDateTime.of(2026, 7, 8, 1, 0, 0, 0, zoneId).toInstant()

        val segments = DialGeometry.arcSegmentsFor(start, end, zoneId)

        assertEquals(2, segments.size)
        assertEquals(345f, segments[0].startAngleDegrees, 0.001f)
        assertEquals(15f, segments[0].sweepDegrees, 0.001f)
        assertEquals(0f, segments[1].startAngleDegrees, 0.001f)
        assertEquals(15f, segments[1].sweepDegrees, 0.001f)
    }
}
