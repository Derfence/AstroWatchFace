package com.derfence.astroface.wear.astro

object SharedAstronomySources {
    val celestialPositionSource: CelestialPositionSource by lazy {
        CachingCelestialPositionSource()
    }
    val constellationSource: ConstellationSource by lazy {
        AstronomyEngineConstellationSource()
    }
    val celestialHorizonSource: CelestialHorizonSource by lazy {
        AstronomyEngineCelestialHorizonSource()
    }
    val astroEventSource: AstroEventSource by lazy {
        CachingAstroEventSource()
    }
    val moonPhaseSource: MoonPhaseSource by lazy {
        AstronomyEngineMoonPhaseSource()
    }
}
