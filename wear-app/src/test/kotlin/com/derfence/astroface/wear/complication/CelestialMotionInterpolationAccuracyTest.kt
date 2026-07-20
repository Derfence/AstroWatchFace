package com.derfence.astroface.wear.complication

import com.derfence.astroface.wear.astro.AstroObserver
import com.derfence.astroface.wear.astro.AstronomyEngineCelestialPositionSource
import java.time.Instant
import kotlin.math.abs
import org.junit.Assert.assertTrue
import org.junit.Test

class CelestialMotionInterpolationAccuracyTest {
    @Test
    fun minuteInterpolationStaysBelowVisualErrorTarget() {
        val source = AstronomyEngineCelestialPositionSource()
        val observer = AstroObserver.DEFAULT
        val anchors = listOf(
            Instant.parse("2026-01-15T00:00:00Z"),
            Instant.parse("2026-04-15T06:00:00Z"),
            Instant.parse("2026-07-15T12:00:00Z"),
            Instant.parse("2026-10-15T18:00:00Z")
        )
        var maximumError = 0.0

        anchors.forEach { anchor ->
            repeat(6) { intervalIndex ->
                val start = anchor.plusSeconds(intervalIndex * 600L)
                val end = start.plusSeconds(600L)
                val startPositions = source.positionsAt(start, observer).positions.associateBy { it.body }
                val endPositions = source.positionsAt(end, observer).positions.associateBy { it.body }
                val decodedByBody = startPositions.keys.associateWith { body ->
                    val sample = CelestialMotionCodec.sample(
                        body,
                        requireNotNull(startPositions[body]).azimuthDegrees,
                        requireNotNull(endPositions[body]).azimuthDegrees
                    )
                    CelestialMotionCodec.decodeField(
                        CelestialMotionCodec.encodePayload(sample).toFloat(),
                        band = 0
                    )
                }

                repeat(11) { minute ->
                    val truth = source.positionsAt(start.plusSeconds(minute * 60L), observer)
                        .positions
                        .associateBy { it.body }
                    decodedByBody.forEach { (body, motion) ->
                        val interpolated = CelestialMotionCodec.interpolate(motion, minute / 10.0)
                        val expected = normalize(requireNotNull(truth[body]).azimuthDegrees - 180.0)
                        maximumError = maxOf(maximumError, circularDistance(interpolated, expected))
                    }
                }
            }
        }

        assertTrue("Maximum interpolation error was $maximumError°", maximumError <= 0.3)
    }

    private fun circularDistance(first: Double, second: Double): Double {
        val difference = abs(first - second) % 360.0
        return minOf(difference, 360.0 - difference)
    }

    private fun normalize(value: Double): Double {
        val remainder = value % 360.0
        return if (remainder < 0.0) remainder + 360.0 else remainder
    }
}
