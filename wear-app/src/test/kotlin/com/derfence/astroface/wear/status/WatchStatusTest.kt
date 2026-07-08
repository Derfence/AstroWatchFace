package com.derfence.astroface.wear.status

import com.derfence.astroface.wear.astro.MoonPhaseKind
import com.derfence.astroface.wear.astro.MoonPhaseSnapshot
import com.derfence.astroface.wear.astro.MoonPhaseSource
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WatchStatusTest {
    @Test
    fun batteryStatusIsBoundedAndLowAtTwentyPercent() {
        assertEquals(100, BatteryStatus.fromPercent(145).percent)
        assertEquals(0, BatteryStatus.fromPercent(-8).percent)
        assertTrue(BatteryStatus.fromPercent(20).isLow)
        assertFalse(BatteryStatus.fromPercent(21).isLow)
    }

    @Test
    fun batteryStatusKeepsUnknownLevelReadable() {
        val status = BatteryStatus.fromPercent(null)

        assertNull(status.percent)
        assertFalse(status.isLow)
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
        assertEquals(MoonPhaseKind.FULL, status.moonPhase.kind)
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
                phaseAngleDegrees = 180.0,
                illuminationPercent = 100,
                kind = MoonPhaseKind.FULL
            )
    }
}
