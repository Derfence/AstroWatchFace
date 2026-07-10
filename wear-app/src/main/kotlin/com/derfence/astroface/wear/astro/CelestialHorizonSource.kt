package com.derfence.astroface.wear.astro

import java.time.Instant
import java.time.LocalDate

enum class CelestialHorizonEventType {
    RISE,
    SET
}

data class CelestialHorizonMarker(
    val body: CelestialBody,
    val type: CelestialHorizonEventType,
    val time: Instant,
    val azimuthDegrees: Double
)

data class CelestialHorizonSnapshot(
    val calculatedAt: Instant,
    val localDate: LocalDate,
    val markers: List<CelestialHorizonMarker>
)

interface CelestialHorizonSource {
    fun horizonMarkersAt(
        time: Instant,
        observer: AstroObserver = AstroObserver.DEFAULT
    ): CelestialHorizonSnapshot
}
