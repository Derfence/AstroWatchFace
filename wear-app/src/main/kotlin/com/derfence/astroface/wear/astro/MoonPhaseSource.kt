package com.derfence.astroface.wear.astro

import java.time.Instant

interface MoonPhaseSource {
    fun phaseAt(time: Instant): MoonPhaseSnapshot
}

data class MoonPhaseSnapshot(
    val calculatedAt: Instant,
    val targetTime: Instant,
    val phaseAngleDegrees: Double,
    val illuminationPercent: Int,
    val validUntil: Instant?
)
