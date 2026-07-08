package com.derfence.astroface.wear.astro

import java.time.Instant
import kotlin.math.floor

interface MoonPhaseSource {
    fun phaseAt(time: Instant): MoonPhaseSnapshot
}

data class MoonPhaseSnapshot(
    val calculatedAt: Instant,
    val phaseAngleDegrees: Double,
    val illuminationPercent: Int,
    val kind: MoonPhaseKind
)

enum class MoonPhaseKind {
    NEW,
    WAXING_CRESCENT,
    FIRST_QUARTER,
    WAXING_GIBBOUS,
    FULL,
    WANING_GIBBOUS,
    LAST_QUARTER,
    WANING_CRESCENT;

    companion object {
        fun fromAngleDegrees(angleDegrees: Double): MoonPhaseKind {
            val normalized = angleDegrees.normalizedDegrees()
            val index = floor((normalized + HALF_PHASE_BUCKET_DEGREES) / PHASE_BUCKET_DEGREES)
                .toInt() % entries.size
            return entries[index]
        }
    }
}

internal fun Double.normalizedDegrees(): Double {
    val remainder = this % FULL_CIRCLE_DEGREES
    return if (remainder < 0.0) remainder + FULL_CIRCLE_DEGREES else remainder
}

private const val FULL_CIRCLE_DEGREES = 360.0
private const val PHASE_BUCKET_DEGREES = 45.0
private const val HALF_PHASE_BUCKET_DEGREES = PHASE_BUCKET_DEGREES / 2.0
