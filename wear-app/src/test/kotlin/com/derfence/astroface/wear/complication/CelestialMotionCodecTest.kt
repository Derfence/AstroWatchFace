package com.derfence.astroface.wear.complication

import com.derfence.astroface.wear.astro.CelestialBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CelestialMotionCodecTest {
    @Test
    fun encodesThreeExactFloatBands() {
        val samples = listOf(
            CelestialMotionSample(CelestialBody.SUN, 0.0, 1.0),
            CelestialMotionSample(CelestialBody.MOON, 180.0, -2.0),
            CelestialMotionSample(CelestialBody.MERCURY, 359.9, 180.0)
        )

        val fields = CelestialMotionCodec.fieldsFor(samples)

        assertTrue(fields.minimum < fields.value)
        assertTrue(fields.value < fields.maximum)
        assertTrue(fields.maximum < (1 shl 24).toFloat())
        assertEquals(fields.minimum.toInt().toFloat(), fields.minimum)
        assertEquals(fields.value.toInt().toFloat(), fields.value)
        assertEquals(fields.maximum.toInt().toFloat(), fields.maximum)
    }

    @Test
    fun usesTheShortestCircularDeltaAcrossNorth() {
        val sample = CelestialMotionCodec.sample(
            body = CelestialBody.SUN,
            startAzimuthDegrees = 179.0,
            endAzimuthDegrees = 181.0
        )
        val decoded = CelestialMotionCodec.decodeField(
            CelestialMotionCodec.encodePayload(sample).toFloat(),
            band = 0
        )

        assertEquals(359.0, decoded.startAngleDegrees, 0.11)
        assertEquals(2.0, decoded.deltaAngleDegrees, 0.11)
        assertEquals(0.0, CelestialMotionCodec.interpolate(decoded, 0.5), 0.11)
    }

    @Test
    fun preservesNegativeMovementWithinQuantization() {
        val original = CelestialMotionSample(CelestialBody.MARS, 123.47, -14.33)
        val decoded = CelestialMotionCodec.decodeField(
            CelestialMotionCodec.encodePayload(original).toFloat(),
            band = 0
        )

        assertEquals(original.startAngleDegrees, decoded.startAngleDegrees, 0.1)
        assertEquals(original.deltaAngleDegrees, decoded.deltaAngleDegrees, 0.1)
    }

    @Test
    fun decodesValueAndMaximumBands() {
        val samples = CelestialMotionGroup.INNER.bodies.mapIndexed { index, body ->
            CelestialMotionSample(body, index * 30.0, index - 1.0)
        }
        val fields = CelestialMotionCodec.fieldsFor(samples)

        assertEquals(
            30.0,
            CelestialMotionCodec.decodeField(fields.value, CelestialMotionCodec.VALUE_BAND)
                .startAngleDegrees,
            0.0
        )
        assertEquals(
            60.0,
            CelestialMotionCodec.decodeField(fields.maximum, CelestialMotionCodec.MAXIMUM_BAND)
                .startAngleDegrees,
            0.0
        )
    }
}
