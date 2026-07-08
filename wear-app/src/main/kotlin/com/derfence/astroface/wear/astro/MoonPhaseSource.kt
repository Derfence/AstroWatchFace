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

internal fun Double.normalizedDegrees(): Double {
    val remainder = this % FULL_CIRCLE_DEGREES
    return if (remainder < 0.0) remainder + FULL_CIRCLE_DEGREES else remainder
}

private const val FULL_CIRCLE_DEGREES = 360.0
