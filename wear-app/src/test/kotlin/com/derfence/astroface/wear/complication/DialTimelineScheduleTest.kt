package com.derfence.astroface.wear.complication

import java.time.Duration
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class DialTimelineScheduleTest {
    @Test
    fun watchModeCoversSixHoursWithThirtyMinuteEntries() {
        val start = Instant.parse("2026-07-12T10:07:00Z")
        val intervals = DialTimelineSchedule.WatchMode.intervalsStartingAt(start)

        assertEquals(12, intervals.size)
        assertEquals(start, intervals.first().start)
        assertEquals(start.plus(Duration.ofMinutes(10)), intervals.first().end)
        assertEquals(start.plus(Duration.ofHours(2)), intervals.last().end)
    }

    @Test
    fun passageModeCoversFortyEightHoursWithDailyEntries() {
        val start = Instant.parse("2026-07-12T10:07:00Z")
        val intervals = DialTimelineSchedule.PassageMode.intervalsStartingAt(start)

        assertEquals(2, intervals.size)
        assertEquals(start.plus(Duration.ofHours(24)), intervals.first().end)
        assertEquals(start.plus(Duration.ofHours(48)), intervals.last().end)
    }
}
