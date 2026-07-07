package com.derfence.astroface.wear.astro

import java.time.Instant
import java.time.ZoneId

data class AstroObserver(
    val latitude: Double,
    val longitude: Double,
    val elevationMeters: Double,
    val zoneId: ZoneId
) {
    companion object {
        val DEFAULT = AstroObserver(
            latitude = 45.665694,
            longitude = 2.944194,
            elevationMeters = 0.0,
            zoneId = ZoneId.of("Europe/Paris")
        )
    }
}

enum class AstroEventType {
    SUNRISE,
    SUNSET,
    ASTRONOMICAL_DAWN,
    ASTRONOMICAL_DUSK,
    MOONRISE,
    MOONSET
}

enum class AstroIntervalType {
    SUNLIGHT,
    ASTRONOMICAL_TWILIGHT,
    ASTRONOMICAL_NIGHT,
    MOON_VISIBLE
}

data class AstroEvent(
    val type: AstroEventType,
    val time: Instant
)

data class AstroInterval(
    val type: AstroIntervalType,
    val start: Instant,
    val end: Instant
)

data class RollingAstroWindow(
    val start: Instant,
    val end: Instant,
    val events: List<AstroEvent>,
    val intervals: List<AstroInterval>
)
