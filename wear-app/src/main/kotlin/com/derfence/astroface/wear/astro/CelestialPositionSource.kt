package com.derfence.astroface.wear.astro

import java.time.Instant

interface CelestialPositionSource {
    fun positionsAt(
        time: Instant,
        observer: AstroObserver = AstroObserver.DEFAULT
    ): CelestialPositionSnapshot
}
