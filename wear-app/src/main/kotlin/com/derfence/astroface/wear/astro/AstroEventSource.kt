package com.derfence.astroface.wear.astro

import java.time.Instant

interface AstroEventSource {
    fun eventsBetween(
        start: Instant,
        end: Instant,
        observer: AstroObserver
    ): List<AstroEvent>

    fun sunAltitudeDegrees(time: Instant, observer: AstroObserver): Double

    fun moonAltitudeDegrees(time: Instant, observer: AstroObserver): Double
}
