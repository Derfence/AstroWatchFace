package com.derfence.astroface.wear.dial

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DialGeometryTest {
    @Test
    fun twentyFourHourDialHasTwentyFourTicks() {
        assertEquals(24, DialGeometry.twentyFourHourTicks().size)
    }

    @Test
    fun twentyFourHourDialLabelsEveryThreeHours() {
        val labels = DialGeometry.twentyFourHourTicks().mapNotNull { it.label }

        assertEquals(listOf("00", "03", "06", "09", "12", "15", "18", "21"), labels)
    }

    @Test
    fun midnightIsAtTop() {
        val midnight = DialGeometry.twentyFourHourTicks().first()
        val point = DialGeometry.point(radius = 100f, angleDegrees = midnight.angleDegrees)

        assertEquals(0f, midnight.angleDegrees, 0.001f)
        assertEquals(DialGeometry.center, point.x, 0.001f)
        assertEquals(DialGeometry.center - 100f, point.y, 0.001f)
    }

    @Test
    fun twelveHourDialHasTwelveIndexesWithoutLabels() {
        val ticks = DialGeometry.twelveHourTicks()

        assertEquals(12, ticks.size)
        ticks.forEach { assertNull(it.label) }
    }
}
