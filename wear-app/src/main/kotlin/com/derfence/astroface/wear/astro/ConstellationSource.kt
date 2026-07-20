package com.derfence.astroface.wear.astro

import java.time.Instant

interface ConstellationSource {
    fun constellationsAt(
        time: Instant,
        observer: AstroObserver = AstroObserver.DEFAULT
    ): ConstellationSnapshot
}

data class ConstellationSnapshot(
    val calculatedAt: Instant,
    val refreshInstant: Instant,
    val targetMidnight: Instant,
    val nextRefreshInstant: Instant,
    val lines: List<ConstellationLine>
)

data class ConstellationLine(
    val constellationId: String,
    val from: SkyPoint,
    val to: SkyPoint
)

data class SkyPoint(
    val azimuthDegrees: Double,
    val zenithDistanceDegrees: Double
)
