package com.derfence.astroface.wear.status

import com.derfence.astroface.wear.astro.MoonPhaseSnapshot

data class WatchStatus(
    val dateLabel: String,
    val battery: BatteryStatus,
    val moonPhase: MoonPhaseSnapshot
)

data class BatteryStatus(
    val percent: Int?
) {
    companion object {
        fun fromPercent(percent: Int?): BatteryStatus {
            val normalized = percent?.coerceIn(MIN_PERCENT, MAX_PERCENT)
            return BatteryStatus(percent = normalized)
        }
    }
}

private const val MIN_PERCENT = 0
private const val MAX_PERCENT = 100
