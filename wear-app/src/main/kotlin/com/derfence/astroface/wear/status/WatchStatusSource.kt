package com.derfence.astroface.wear.status

import com.derfence.astroface.wear.astro.AstronomyEngineMoonPhaseSource
import com.derfence.astroface.wear.astro.MoonPhaseSource
import java.time.Instant

interface WatchStatusSource {
    fun statusAt(time: Instant): WatchStatus
}

interface BatteryStatusSource {
    fun currentBatteryStatus(): BatteryStatus
}

class DefaultWatchStatusSource(
    private val batteryStatusSource: BatteryStatusSource,
    private val moonPhaseSource: MoonPhaseSource = AstronomyEngineMoonPhaseSource(),
    private val dateStatusFormatter: DateStatusFormatter = DateStatusFormatter()
) : WatchStatusSource {
    override fun statusAt(time: Instant): WatchStatus =
        WatchStatus(
            dateLabel = dateStatusFormatter.labelFor(time),
            battery = batteryStatusSource.currentBatteryStatus(),
            moonPhase = moonPhaseSource.phaseAt(time)
        )
}
