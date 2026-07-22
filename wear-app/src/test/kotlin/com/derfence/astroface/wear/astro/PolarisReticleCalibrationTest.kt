package com.derfence.astroface.wear.astro

import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.Observer
import io.github.cosinekitty.astronomy.Time
import io.github.cosinekitty.astronomy.defineStar
import io.github.cosinekitty.astronomy.hourAngle
import java.time.Duration
import java.time.Instant
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test

class PolarisReticleCalibrationTest {
    @Test
    fun wffCalibrationStaysWithinOneClockMinuteFrom2020Through2040() {
        var instant = Instant.parse("2020-01-01T00:00:00Z")
        val end = Instant.parse("2041-01-01T00:00:00Z")
        var maximumErrorDegrees = 0.0

        while (instant.isBefore(end)) {
            maximumErrorDegrees = maxOf(
                maximumErrorDegrees,
                abs(shortestDeltaDegrees(wffAngleDegrees(instant), oracleAngleDegrees(instant)))
            )
            instant = instant.plus(Duration.ofDays(14))
        }

        assertTrue(
            "Maximum error was $maximumErrorDegrees°, expected at most one clock minute (0.5°).",
            maximumErrorDegrees <= MAXIMUM_ERROR_DEGREES
        )
    }

    @Test
    fun cardinalReticlePositionsFollowSynScanClockConvention() {
        assertEquals(180.0, reticleAngleForHourAngle(0.0), 0.0001)
        assertEquals(90.0, reticleAngleForHourAngle(6.0), 0.0001)
        assertEquals(0.0, reticleAngleForHourAngle(12.0), 0.0001)
        assertEquals(270.0, reticleAngleForHourAngle(18.0), 0.0001)
    }

    @Test
    fun twelveOClockWrapIsContinuous() {
        val beforeWrap = wffAngleDegrees(Instant.parse("2026-01-01T08:09:00Z"))
        val afterWrap = wffAngleDegrees(Instant.parse("2026-01-01T08:10:00Z"))

        assertTrue(beforeWrap in 0.0..0.5)
        assertTrue(afterWrap in 359.5..360.0)
        assertEquals(
            -360.0 * 60_000.0 / SIDEREAL_DAY_MILLIS,
            shortestDeltaDegrees(afterWrap, beforeWrap),
            0.001
        )
    }

    private fun oracleAngleDegrees(instant: Instant): Double {
        val hourAngleHours = hourAngle(
            Body.Star1,
            Time.fromMillisecondsSince1970(instant.toEpochMilli()),
            Observer(
                AstroObserver.DEFAULT.latitude,
                AstroObserver.DEFAULT.longitude,
                AstroObserver.DEFAULT.elevationMeters
            )
        )
        return normalizeDegrees(180.0 - hourAngleHours * 15.0)
    }

    private fun wffAngleDegrees(instant: Instant): Double {
        val elapsedMillis = instant.toEpochMilli() - REFERENCE_EPOCH_MILLIS
        val cycleMillis = Math.floorMod(elapsedMillis, SIDEREAL_DAY_MILLIS)
        val elapsedDays = Math.floorDiv(elapsedMillis, CIVIL_DAY_MILLIS)
        return normalizeDegrees(
            REFERENCE_ANGLE_DEGREES -
                360.0 * cycleMillis / SIDEREAL_DAY_MILLIS +
                SECULAR_CORRECTION_DEGREES_PER_DAY * elapsedDays
        )
    }

    private fun reticleAngleForHourAngle(hourAngleHours: Double): Double =
        normalizeDegrees((6.0 - hourAngleHours / 2.0) * 30.0)

    private fun shortestDeltaDegrees(first: Double, second: Double): Double =
        normalizeDegrees(first - second + 180.0) - 180.0

    private fun normalizeDegrees(value: Double): Double {
        val remainder = value % 360.0
        return if (remainder < 0.0) remainder + 360.0 else remainder
    }

    private companion object {
        const val POLARIS_RA_J2000_HOURS = 2.530301028
        const val POLARIS_DEC_J2000_DEGREES = 89.26410944
        const val POLARIS_DISTANCE_LIGHT_YEARS = 433.0
        const val REFERENCE_EPOCH_MILLIS = 1_767_225_600_000L
        const val REFERENCE_ANGLE_DEGREES = 122.754941129
        const val SIDEREAL_DAY_MILLIS = 86_164_091L
        const val CIVIL_DAY_MILLIS = 86_400_000L
        const val SECULAR_CORRECTION_DEGREES_PER_DAY = 0.001125874
        const val MAXIMUM_ERROR_DEGREES = 0.5

        @JvmStatic
        @BeforeClass
        fun definePolaris() {
            defineStar(
                Body.Star1,
                POLARIS_RA_J2000_HOURS,
                POLARIS_DEC_J2000_DEGREES,
                POLARIS_DISTANCE_LIGHT_YEARS
            )
        }
    }
}
