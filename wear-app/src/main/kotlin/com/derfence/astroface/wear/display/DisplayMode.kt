package com.derfence.astroface.wear.display

enum class DisplayMode {
    FULL_DIAL,
    CONSTELLATIONS_NIGHT,
    SOLAR_SYSTEM;

    fun next(): DisplayMode =
        when (this) {
            FULL_DIAL -> CONSTELLATIONS_NIGHT
            CONSTELLATIONS_NIGHT -> SOLAR_SYSTEM
            SOLAR_SYSTEM -> FULL_DIAL
        }
}
