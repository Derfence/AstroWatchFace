package com.derfence.astroface.wear.status

import com.derfence.astroface.wear.astro.MoonPhaseSnapshot
import com.derfence.astroface.wear.astro.MoonPhaseSource
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class WatchStatusTest {
    @Test
    fun defaultWatchStatusCombinesDateAndMoonPhase() {
        val time = Instant.parse("2026-07-04T10:00:00Z")
        val status = DefaultWatchStatusSource(
            moonPhaseSource = FixedMoonPhaseSource(time),
            dateStatusFormatter = DateStatusFormatter()
        ).statusAt(time)

        assertEquals("sam. 04 juil.", status.dateLabel)
        assertEquals(180.0, status.moonPhase.phaseAngleDegrees, 0.001)
    }

    private class FixedMoonPhaseSource(
        private val calculatedAt: Instant
    ) : MoonPhaseSource {
        override fun phaseAt(time: Instant): MoonPhaseSnapshot =
            MoonPhaseSnapshot(
                calculatedAt = calculatedAt,
                targetTime = calculatedAt,
                phaseAngleDegrees = 180.0,
                illuminationPercent = 100,
                validUntil = null
            )
    }
}
