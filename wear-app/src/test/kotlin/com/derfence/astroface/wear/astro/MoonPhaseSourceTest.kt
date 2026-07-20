package com.derfence.astroface.wear.astro

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MoonPhaseSourceTest {
    @Test
    fun phaseTargetsNextMoonriseAfterPreviousMoonset() {
        val now = Instant.parse("2026-07-08T12:00:00Z")
        val previousMoonset = Instant.parse("2026-07-08T05:00:00Z")
        val targetMoonrise = Instant.parse("2026-07-08T21:00:00Z")
        val nextMoonset = Instant.parse("2026-07-09T06:00:00Z")
        val source = AstronomyEngineMoonPhaseSource(
            observer = AstroObserver.DEFAULT,
            moonRiseSetSource = FakeMoonRiseSetSource(
                previousMoonset = previousMoonset,
                nextMoonset = nextMoonset,
                nextMoonrise = targetMoonrise
            ),
            moonPhaseCalculator = FakeMoonPhaseCalculator()
        )

        val phase = source.phaseAt(now)

        assertEquals(now, phase.calculatedAt)
        assertEquals(targetMoonrise, phase.targetTime)
        assertEquals(nextMoonset, phase.validUntil)
        assertEquals(123.0, phase.phaseAngleDegrees, 0.001)
        assertEquals(67, phase.illuminationPercent)
    }

    @Test
    fun astronomyEnginePhaseSnapshotIsNormalized() {
        val snapshot = AstronomyEngineMoonPhaseSource()
            .phaseAt(Instant.parse("2026-07-07T22:00:00Z"))

        assertEquals(Instant.parse("2026-07-07T22:00:00Z"), snapshot.calculatedAt)
        assertTrue(snapshot.targetTime.isAfter(snapshot.calculatedAt))
        assertTrue(snapshot.phaseAngleDegrees >= 0.0)
        assertTrue(snapshot.phaseAngleDegrees < 360.0)
        assertTrue(snapshot.illuminationPercent in 0..100)
    }

    @Test
    fun repeatedCallsBeforeValidUntilReuseMoonEventsAndPhase() {
        val events = FakeMoonRiseSetSource(
            previousMoonset = Instant.parse("2026-07-08T05:00:00Z"),
            nextMoonset = Instant.parse("2026-07-09T06:00:00Z"),
            nextMoonrise = Instant.parse("2026-07-08T21:00:00Z")
        )
        val calculator = FakeMoonPhaseCalculator()
        val source = AstronomyEngineMoonPhaseSource(
            observer = AstroObserver.DEFAULT,
            moonRiseSetSource = events,
            moonPhaseCalculator = calculator
        )

        source.phaseAt(Instant.parse("2026-07-08T12:00:00Z"))
        source.phaseAt(Instant.parse("2026-07-08T18:00:00Z"))

        assertEquals(1, events.previousMoonsetCalls)
        assertEquals(1, events.nextMoonriseCalls)
        assertEquals(1, events.nextMoonsetCalls)
        assertEquals(1, calculator.calls)
    }

    private class FakeMoonRiseSetSource(
        private val previousMoonset: Instant?,
        private val nextMoonset: Instant?,
        private val nextMoonrise: Instant?
    ) : MoonRiseSetSource {
        var previousMoonsetCalls = 0
            private set
        var nextMoonsetCalls = 0
            private set
        var nextMoonriseCalls = 0
            private set

        override fun previousMoonsetAtOrBefore(time: Instant, observer: AstroObserver): Instant? =
            previousMoonset.also { previousMoonsetCalls += 1 }

        override fun nextMoonsetAfter(time: Instant, observer: AstroObserver): Instant? =
            nextMoonset.also { nextMoonsetCalls += 1 }

        override fun nextMoonriseAfter(time: Instant, observer: AstroObserver): Instant? =
            nextMoonrise.also { nextMoonriseCalls += 1 }
    }

    private class FakeMoonPhaseCalculator : MoonPhaseCalculator {
        var calls = 0
            private set

        override fun phaseAt(time: Instant): MoonPhaseValues =
            MoonPhaseValues(
                phaseAngleDegrees = 123.0,
                illuminationPercent = 67
            ).also { calls += 1 }
    }
}
