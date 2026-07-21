package com.derfence.astroface.wear.complication

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.wear.watchface.complications.data.ComplicationType
import com.derfence.astroface.wear.astro.AstroObserver
import com.derfence.astroface.wear.astro.CachingCelestialPositionSource
import com.derfence.astroface.wear.astro.CelestialBody
import com.derfence.astroface.wear.astro.CelestialPosition
import com.derfence.astroface.wear.astro.CelestialPositionSnapshot
import com.derfence.astroface.wear.astro.CelestialPositionSource
import java.time.Instant
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CelestialMotionComplicationDataFactoryInstrumentedTest {
    @Test
    fun threeProvidersShareBoundaryCalculationsAndReturnRangedTimelines() {
        val delegate = CountingSource()
        val cachedSource = CachingCelestialPositionSource(delegate, maxEntries = 128)
        val start = Instant.parse("2026-07-20T10:00:00Z")

        CelestialMotionGroup.entries.forEach { group ->
            val timeline = CelestialMotionComplicationDataFactory.createTimeline(
                group = group,
                start = start,
                zoneId = ZoneOffset.UTC,
                positionSource = cachedSource
            )
            assertEquals(60, timeline.timelineEntries.size)
            assertEquals(ComplicationType.RANGED_VALUE, timeline.defaultComplicationData.type)
            assertSame(
                timeline.defaultComplicationData,
                timeline.timelineEntries.first().complicationData
            )
        }

        assertEquals(61, delegate.calls)
    }

    @Test
    fun unalignedRequestUsesAtMostSixtyTwoBoundaryCalculations() {
        val delegate = CountingSource()
        val cachedSource = CachingCelestialPositionSource(delegate, maxEntries = 128)
        val start = Instant.parse("2026-07-20T10:07:25Z")

        val timeline = CelestialMotionComplicationDataFactory.createTimeline(
            group = CelestialMotionGroup.INNER,
            start = start,
            zoneId = ZoneOffset.UTC,
            positionSource = cachedSource
        )

        assertEquals(61, timeline.timelineEntries.size)
        assertEquals(62, delegate.calls)
    }

    @Test
    fun eightHourRefreshReusesTheTwoHourOverlap() {
        val delegate = CountingSource()
        val cachedSource = CachingCelestialPositionSource(delegate, maxEntries = 128)
        val start = Instant.parse("2026-07-20T10:00:00Z")

        CelestialMotionGroup.entries.forEach { group ->
            CelestialMotionComplicationDataFactory.createTimeline(
                group = group,
                start = start,
                zoneId = ZoneOffset.UTC,
                positionSource = cachedSource
            )
        }
        CelestialMotionGroup.entries.forEach { group ->
            val timeline = CelestialMotionComplicationDataFactory.createTimeline(
                group = group,
                start = start.plusSeconds(8 * 3600L),
                zoneId = ZoneOffset.UTC,
                positionSource = cachedSource
            )
            assertEquals(60, timeline.timelineEntries.size)
        }

        assertEquals(109, delegate.calls)
    }

    private class CountingSource : CelestialPositionSource {
        var calls = 0

        override fun positionsAt(
            time: Instant,
            observer: AstroObserver
        ): CelestialPositionSnapshot {
            calls += 1
            return CelestialPositionSnapshot(
                calculatedAt = time,
                positions = CelestialBody.entries.mapIndexed { index, body ->
                    CelestialPosition(body, (index * 37.0 + calls) % 360.0)
                }
            )
        }
    }
}
