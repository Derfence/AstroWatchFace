package com.derfence.astroface.wear.display

import org.junit.Assert.assertEquals
import org.junit.Test

class DisplayModeTest {
    @Test
    fun modesCycleInExpectedOrder() {
        assertEquals(DisplayMode.CONSTELLATIONS_NIGHT, DisplayMode.FULL_DIAL.next())
        assertEquals(DisplayMode.SOLAR_SYSTEM, DisplayMode.CONSTELLATIONS_NIGHT.next())
        assertEquals(DisplayMode.FULL_DIAL, DisplayMode.SOLAR_SYSTEM.next())
    }
}
