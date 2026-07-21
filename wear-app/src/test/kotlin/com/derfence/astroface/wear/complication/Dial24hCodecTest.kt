package com.derfence.astroface.wear.complication

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Dial24hCodecTest {
    @Test
    fun roundTripsCivilMinuteBoundsAndSentinelsExactly() {
        val projections = listOf(
            Dial24hProjection(
                astronomicalDawn = Dial24hBoundary.Minute(0),
                sunrise = Dial24hBoundary.Minute(1439),
                sunset = Dial24hBoundary.FullDay,
                astronomicalDusk = Dial24hBoundary.Now,
                moonVisibleStart = Dial24hBoundary.Absent,
                moonVisibleEnd = Dial24hBoundary.Minute(0)
            ),
            Dial24hProjection(
                astronomicalDawn = Dial24hBoundary.Absent,
                sunrise = Dial24hBoundary.Now,
                sunset = Dial24hBoundary.FullDay,
                astronomicalDusk = Dial24hBoundary.Minute(1439),
                moonVisibleStart = Dial24hBoundary.Now,
                moonVisibleEnd = Dial24hBoundary.Absent
            )
        )

        projections.forEach { projection ->
            val fields = Dial24hCodec.fieldsFor(projection)

            assertEquals(projection, Dial24hCodec.projectionFrom(fields))
            assertTrue(fields.minimum < fields.value)
            assertTrue(fields.value < fields.maximum)
            assertTrue(fields.maximum < (1 shl 24).toFloat())
            assertEquals(fields.minimum.toInt().toFloat(), fields.minimum)
            assertEquals(fields.value.toInt().toFloat(), fields.value)
            assertEquals(fields.maximum.toInt().toFloat(), fields.maximum)
        }
    }

    @Test
    fun everySupportedCodeRoundTripsInBothPairPositions() {
        val boundaries = buildList {
            repeat(24 * 60) { add(Dial24hBoundary.Minute(it)) }
            add(Dial24hBoundary.FullDay)
            add(Dial24hBoundary.Now)
            add(Dial24hBoundary.Absent)
        }

        boundaries.forEach { boundary ->
            val projection = Dial24hProjection(
                astronomicalDawn = boundary,
                sunrise = boundary,
                sunset = boundary,
                astronomicalDusk = boundary,
                moonVisibleStart = boundary,
                moonVisibleEnd = boundary
            )

            assertEquals(projection, Dial24hCodec.projectionFrom(Dial24hCodec.fieldsFor(projection)))
        }
    }
}
