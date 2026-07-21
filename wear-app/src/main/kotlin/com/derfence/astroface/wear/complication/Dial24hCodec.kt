package com.derfence.astroface.wear.complication

sealed interface Dial24hBoundary {
    data class Minute(val minuteOfDay: Int) : Dial24hBoundary {
        init {
            require(minuteOfDay in 0 until MINUTES_PER_DAY) {
                "Minute of day must be between 0 and ${MINUTES_PER_DAY - 1}."
            }
        }
    }

    data object FullDay : Dial24hBoundary
    data object Now : Dial24hBoundary
    data object Absent : Dial24hBoundary

    companion object {
        private const val MINUTES_PER_DAY = 24 * 60
    }
}

data class Dial24hProjection(
    val astronomicalDawn: Dial24hBoundary,
    val sunrise: Dial24hBoundary,
    val sunset: Dial24hBoundary,
    val astronomicalDusk: Dial24hBoundary,
    val moonVisibleStart: Dial24hBoundary,
    val moonVisibleEnd: Dial24hBoundary
)

data class Dial24hFields(
    val minimum: Float,
    val value: Float,
    val maximum: Float
)

object Dial24hCodec {
    const val FULL_DAY_CODE = 2045
    const val NOW_CODE = 2046
    const val ABSENT_CODE = 2047
    const val PAIR_BASE = 2048
    const val VALUE_OFFSET = 4_194_304
    const val MAXIMUM_OFFSET = 8_388_608

    fun fieldsFor(projection: Dial24hProjection): Dial24hFields =
        Dial24hFields(
            minimum = pack(projection.astronomicalDawn, projection.sunrise).toFloat(),
            value = (VALUE_OFFSET + pack(projection.sunset, projection.astronomicalDusk)).toFloat(),
            maximum = (MAXIMUM_OFFSET +
                pack(projection.moonVisibleStart, projection.moonVisibleEnd)).toFloat()
        ).also { fields ->
            require(fields.minimum < fields.value && fields.value < fields.maximum) {
                "RANGED_VALUE fields must be strictly ordered."
            }
        }

    internal fun projectionFrom(fields: Dial24hFields): Dial24hProjection {
        val dawnSunrise = unpack(fields.minimum.toInt())
        val sunsetDusk = unpack(fields.value.toInt() - VALUE_OFFSET)
        val moon = unpack(fields.maximum.toInt() - MAXIMUM_OFFSET)
        return Dial24hProjection(
            astronomicalDawn = dawnSunrise.first,
            sunrise = dawnSunrise.second,
            sunset = sunsetDusk.first,
            astronomicalDusk = sunsetDusk.second,
            moonVisibleStart = moon.first,
            moonVisibleEnd = moon.second
        )
    }

    private fun pack(first: Dial24hBoundary, second: Dial24hBoundary): Int =
        codeFor(first) * PAIR_BASE + codeFor(second)

    private fun unpack(payload: Int): Pair<Dial24hBoundary, Dial24hBoundary> {
        require(payload in 0 until VALUE_OFFSET) { "Pair payload must fit in 22 bits." }
        return boundaryFor(payload / PAIR_BASE) to boundaryFor(payload % PAIR_BASE)
    }

    private fun codeFor(boundary: Dial24hBoundary): Int =
        when (boundary) {
            is Dial24hBoundary.Minute -> boundary.minuteOfDay
            Dial24hBoundary.FullDay -> FULL_DAY_CODE
            Dial24hBoundary.Now -> NOW_CODE
            Dial24hBoundary.Absent -> ABSENT_CODE
        }

    private fun boundaryFor(code: Int): Dial24hBoundary =
        when (code) {
            in 0 until MINUTES_PER_DAY -> Dial24hBoundary.Minute(code)
            FULL_DAY_CODE -> Dial24hBoundary.FullDay
            NOW_CODE -> Dial24hBoundary.Now
            ABSENT_CODE -> Dial24hBoundary.Absent
            else -> error("Unsupported dial boundary code: $code")
        }

    private const val MINUTES_PER_DAY = 24 * 60
}
