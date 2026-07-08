package com.derfence.astroface.wear.status

import com.derfence.astroface.wear.astro.MoonPhaseSnapshot
import com.derfence.astroface.wear.astro.MoonPhaseSource
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WatchStatusTest {
    @Test
    fun batteryStatusIsBounded() {
        assertEquals(100, BatteryStatus.fromPercent(145).percent)
        assertEquals(0, BatteryStatus.fromPercent(-8).percent)
    }

    @Test
    fun batteryStatusKeepsUnknownLevelReadable() {
        val status = BatteryStatus.fromPercent(null)

        assertNull(status.percent)
    }

    @Test
    fun defaultWatchStatusCombinesDateBatteryAndMoonPhase() {
        val time = Instant.parse("2026-07-04T10:00:00Z")
        val status = DefaultWatchStatusSource(
            batteryStatusSource = FixedBatteryStatusSource(BatteryStatus.fromPercent(83)),
            moonPhaseSource = FixedMoonPhaseSource(time),
            dateStatusFormatter = DateStatusFormatter()
        ).statusAt(time)

        assertEquals("sam. 04 juil.", status.dateLabel)
        assertEquals(83, status.battery.percent)
        assertEquals(180.0, status.moonPhase.phaseAngleDegrees, 0.001)
    }

    private class FixedBatteryStatusSource(
        private val batteryStatus: BatteryStatus
    ) : BatteryStatusSource {
        override fun currentBatteryStatus(): BatteryStatus = batteryStatus
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
