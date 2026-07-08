package com.derfence.astroface.wear.astro

import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.illumination
import io.github.cosinekitty.astronomy.moonPhase
import java.time.Instant
import kotlin.math.roundToInt

class AstronomyEngineMoonPhaseSource : MoonPhaseSource {
    override fun phaseAt(time: Instant): MoonPhaseSnapshot {
        val astronomyTime = time.toAstronomyTime()
        val phaseAngleDegrees = moonPhase(astronomyTime).normalizedDegrees()
        val illuminationPercent = (illumination(Body.Moon, astronomyTime).phaseFraction * 100.0)
            .roundToInt()
            .coerceIn(MIN_PERCENT, MAX_PERCENT)

        return MoonPhaseSnapshot(
            calculatedAt = time,
            phaseAngleDegrees = phaseAngleDegrees,
            illuminationPercent = illuminationPercent,
            kind = MoonPhaseKind.fromAngleDegrees(phaseAngleDegrees)
        )
    }

    private companion object {
        private const val MIN_PERCENT = 0
        private const val MAX_PERCENT = 100
    }
}
