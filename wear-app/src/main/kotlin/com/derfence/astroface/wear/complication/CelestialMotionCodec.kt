package com.derfence.astroface.wear.complication

import com.derfence.astroface.wear.astro.CelestialBody
import kotlin.math.roundToInt

data class CelestialMotionSample(
    val body: CelestialBody,
    val startAngleDegrees: Double,
    val deltaAngleDegrees: Double
)

data class CelestialMotionFields(
    val minimum: Float,
    val value: Float,
    val maximum: Float
)

data class DecodedCelestialMotion(
    val startAngleDegrees: Double,
    val deltaAngleDegrees: Double
)

enum class CelestialMotionGroup(
    val bodies: List<CelestialBody>,
    val contentDescription: String
) {
    INNER(
        listOf(CelestialBody.SUN, CelestialBody.MOON, CelestialBody.MERCURY),
        "Positions du Soleil, de la Lune et de Mercure"
    ),
    MIDDLE(
        listOf(CelestialBody.VENUS, CelestialBody.MARS, CelestialBody.JUPITER),
        "Positions de Vénus, de Mars et de Jupiter"
    ),
    OUTER(
        listOf(CelestialBody.SATURN, CelestialBody.URANUS, CelestialBody.NEPTUNE),
        "Positions de Saturne, d'Uranus et de Neptune"
    )
}

object CelestialMotionCodec {
    const val QUANTIZATION_DEGREES = 0.2
    const val PAYLOAD_RADIX = 2048
    const val DELTA_OFFSET = 900
    const val VALUE_BAND = 4_194_304
    const val MAXIMUM_BAND = 8_388_608

    fun sample(
        body: CelestialBody,
        startAzimuthDegrees: Double,
        endAzimuthDegrees: Double
    ): CelestialMotionSample {
        val startAngle = displayAngleForAzimuth(startAzimuthDegrees)
        val endAngle = displayAngleForAzimuth(endAzimuthDegrees)
        return CelestialMotionSample(
            body = body,
            startAngleDegrees = startAngle,
            deltaAngleDegrees = shortestDelta(startAngle, endAngle)
        )
    }

    fun fieldsFor(samples: List<CelestialMotionSample>): CelestialMotionFields {
        require(samples.size == 3) { "A motion complication must contain exactly three bodies." }
        val payloads = samples.map(::encodePayload)
        return CelestialMotionFields(
            minimum = payloads[0].toFloat(),
            value = (VALUE_BAND + payloads[1]).toFloat(),
            maximum = (MAXIMUM_BAND + payloads[2]).toFloat()
        )
    }

    fun encodePayload(sample: CelestialMotionSample): Int {
        val startQuantized = (normalize(sample.startAngleDegrees) / QUANTIZATION_DEGREES)
            .roundToInt()
            .floorMod(START_ANGLE_STEPS)
        val deltaQuantized = (sample.deltaAngleDegrees / QUANTIZATION_DEGREES)
            .roundToInt()
            .coerceIn(-DELTA_OFFSET, DELTA_OFFSET)
        val payload = startQuantized * PAYLOAD_RADIX + deltaQuantized + DELTA_OFFSET
        check(payload in 0 until VALUE_BAND) { "Celestial motion payload exceeds its float band." }
        return payload
    }

    fun decodeField(field: Float, band: Int): DecodedCelestialMotion {
        val payload = field.roundToInt() - band
        require(payload in 0 until VALUE_BAND) { "Invalid celestial motion field." }
        val startQuantized = payload / PAYLOAD_RADIX
        val deltaQuantized = payload % PAYLOAD_RADIX - DELTA_OFFSET
        return DecodedCelestialMotion(
            startAngleDegrees = startQuantized * QUANTIZATION_DEGREES,
            deltaAngleDegrees = deltaQuantized * QUANTIZATION_DEGREES
        )
    }

    fun interpolate(motion: DecodedCelestialMotion, fraction: Double): Double =
        normalize(motion.startAngleDegrees + motion.deltaAngleDegrees * fraction)

    private fun displayAngleForAzimuth(azimuthDegrees: Double): Double =
        normalize(azimuthDegrees - SOUTH_AZIMUTH_DEGREES)

    private fun shortestDelta(startDegrees: Double, endDegrees: Double): Double =
        normalize(endDegrees - startDegrees + HALF_CIRCLE_DEGREES) - HALF_CIRCLE_DEGREES

    private fun normalize(angleDegrees: Double): Double {
        val remainder = angleDegrees % FULL_CIRCLE_DEGREES
        return if (remainder < 0.0) remainder + FULL_CIRCLE_DEGREES else remainder
    }

    private fun Int.floorMod(modulus: Int): Int = ((this % modulus) + modulus) % modulus

    private const val START_ANGLE_STEPS = 1800
    private const val SOUTH_AZIMUTH_DEGREES = 180.0
    private const val HALF_CIRCLE_DEGREES = 180.0
    private const val FULL_CIRCLE_DEGREES = 360.0
}
