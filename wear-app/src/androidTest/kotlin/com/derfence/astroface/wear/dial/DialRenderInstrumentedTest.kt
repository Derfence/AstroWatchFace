package com.derfence.astroface.wear.dial

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.wear.watchface.complications.data.ComplicationType
import com.derfence.astroface.wear.astro.AstroEvent
import com.derfence.astroface.wear.astro.AstroEventSource
import com.derfence.astroface.wear.astro.AstroObserver
import com.derfence.astroface.wear.astro.AstroWindowCalculator
import com.derfence.astroface.wear.astro.CelestialBody
import com.derfence.astroface.wear.astro.CelestialHorizonEventType
import com.derfence.astroface.wear.astro.CelestialHorizonMarker
import com.derfence.astroface.wear.astro.CelestialHorizonSnapshot
import com.derfence.astroface.wear.astro.CelestialHorizonSource
import com.derfence.astroface.wear.astro.CelestialPosition
import com.derfence.astroface.wear.astro.CelestialPositionSnapshot
import com.derfence.astroface.wear.astro.CelestialPositionSource
import com.derfence.astroface.wear.astro.ConstellationLine
import com.derfence.astroface.wear.astro.ConstellationSnapshot
import com.derfence.astroface.wear.astro.ConstellationSource
import com.derfence.astroface.wear.astro.MoonPhaseSnapshot
import com.derfence.astroface.wear.astro.SolarSystemBody
import com.derfence.astroface.wear.astro.SolarSystemPosition
import com.derfence.astroface.wear.astro.SolarSystemPositionSource
import com.derfence.astroface.wear.astro.SolarSystemSnapshot
import com.derfence.astroface.wear.astro.SkyPoint
import com.derfence.astroface.wear.complication.DialComplicationDataFactory
import com.derfence.astroface.wear.complication.DialTimelineSchedule
import com.derfence.astroface.wear.complication.DialTimelinePlan
import java.time.Duration
import com.derfence.astroface.wear.display.DisplayMode
import com.derfence.astroface.wear.status.WatchStatus
import com.derfence.astroface.wear.status.WatchStatusSource
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DialRenderInstrumentedTest {
    @Test
    fun renderersProduceVisiblePixels() {
        assertTrue(Dial24hRenderer().render().hasVisiblePixel())
        assertTrue(
            ConstellationLayerRenderer(
                style = ConstellationLayerStyle.MAIN,
                constellationSource = FakeConstellationSource()
            ).render().hasVisiblePixel()
        )
        assertTrue(
            CelestialOverlayRenderer(
                clock = Clock.fixed(Instant.parse("2026-07-07T10:00:00Z"), ZoneOffset.UTC),
                positionSource = FakeCelestialPositionSource()
            ).render().hasVisiblePixel()
        )
        assertTrue(
            StatusOverlayRenderer(
                clock = Clock.fixed(Instant.parse("2026-07-07T10:00:00Z"), ZoneOffset.UTC),
                statusSource = FakeWatchStatusSource()
            ).render().hasVisiblePixel()
        )
    }

    @Test
    fun complicationFactoryReturnsPhotoImageData() {
        val time = Instant.parse("2026-07-07T10:00:00Z")
        val data = DialComplicationDataFactory.create(
            Dial24hRenderer(),
            time
        )

        assertEquals(ComplicationType.PHOTO_IMAGE, data.type)
    }

    @Test
    fun complicationFactoryReturnsTimelineEntries() {
        val time = Instant.parse("2026-07-07T10:00:00Z")
        val timeline = DialComplicationDataFactory.createTimeline(
            renderer = Dial24hRenderer(),
            start = time,
            schedule = DialTimelineSchedule.WatchMode
        )

        assertEquals(12, timeline.timelineEntries.size)
        assertEquals(time, timeline.timelineEntries.first().validity.start)
        assertSame(timeline.defaultComplicationData, timeline.timelineEntries.first().complicationData)
    }

    @Test
    fun timelineRendersEachFixedInstantExactlyOnce() {
        val renderer = CountingRenderer()

        DialComplicationDataFactory.createTimeline(
            renderer = renderer,
            start = Instant.parse("2026-07-07T10:00:00Z"),
            schedule = DialTimelineSchedule.WatchMode
        )

        assertEquals(12, renderer.renderCount)
    }

    @Test
    fun validityTimelineUsesContiguousRendererBoundaries() {
        val start = Instant.parse("2026-07-07T10:00:00Z")
        val timeline = DialComplicationDataFactory.createTimeline(
            renderer = ValidityRenderer(Duration.ofHours(12)),
            start = start,
            plan = DialTimelinePlan.ByValidity(Duration.ofHours(48))
        )

        assertEquals(4, timeline.timelineEntries.size)
        timeline.timelineEntries.zipWithNext().forEach { (current, next) ->
            assertEquals(current.validity.end, next.validity.start)
        }
        assertEquals(start.plus(Duration.ofHours(48)), timeline.timelineEntries.last().validity.end)
    }

    @Test
    fun dailyConstellationFrameReusesBitmapInsideRefreshWindow() {
        val renderer = ConstellationLayerRenderer(
            style = ConstellationLayerStyle.MAIN,
            constellationSource = FakeConstellationSource()
        )

        val first = renderer.renderFrameAt(Instant.parse("2026-07-07T10:00:00Z"))
        val second = renderer.renderFrameAt(Instant.parse("2026-07-07T20:00:00Z"))

        assertSame(first.bitmap, second.bitmap)
        assertEquals(first.contentKey, second.contentKey)
    }

    @Test
    fun croppedOverlaysReduceRawBitmapFootprint() {
        val instant = Instant.parse("2026-07-07T10:00:00Z")
        val celestial = CelestialOverlayRenderer(
            positionSource = FakeCelestialPositionSource()
        ).renderAt(instant)
        val horizon = CelestialHorizonOverlayRenderer(
            horizonSource = FakeCelestialHorizonSource()
        ).renderAt(instant)
        val status = StatusOverlayRenderer(
            statusSource = FakeWatchStatusSource()
        ).renderAt(instant)

        assertEquals(340, celestial.width)
        assertEquals(340, celestial.height)
        assertEquals(340, horizon.width)
        assertEquals(340, horizon.height)
        assertEquals(200, status.width)
        assertEquals(190, status.height)
        val previousBytes = 3L * 13L * 450L * 450L * 4L
        val optimizedBytes =
            12L * 450L * 450L * 4L +
                12L * 340L * 340L * 4L +
                3L * 200L * 190L * 4L +
                450L * 450L * 4L +
                340L * 340L * 4L
        assertTrue(optimizedBytes <= previousBytes * 60L / 100L)
    }

    @Test
    fun dailyLayersExposeExactValidityBoundaries() {
        val instant = Instant.parse("2026-07-07T10:00:00Z")
        val constellation = ConstellationLayerRenderer(
            style = ConstellationLayerStyle.MAIN,
            constellationSource = FakeConstellationSource()
        ).renderFrameAt(instant)
        val horizon = CelestialHorizonOverlayRenderer(
            horizonSource = FakeCelestialHorizonSource()
        ).renderFrameAt(instant)

        assertEquals(Instant.parse("2026-07-08T04:00:00Z"), constellation.validUntil)
        assertEquals(Instant.parse("2026-07-07T22:00:00Z"), horizon.validUntil)
    }

    @Test
    fun render24hShowsInjectedAstroArcColor() {
        val renderer = Dial24hRenderer(
            clock = Clock.fixed(Instant.parse("2026-07-07T10:00:00Z"), ZoneOffset.UTC),
            astroWindowCalculator = AstroWindowCalculator(FakeAstroEventSource())
        )

        val bitmap = renderer.render()

        assertTrue(bitmap.hasWarmAstroArcPixel())
    }

    @Test
    fun celestialOverlayPlacesSouthAzimuthAtTop() {
        val bitmap = CelestialOverlayRenderer(
            clock = Clock.fixed(Instant.parse("2026-07-07T10:00:00Z"), ZoneOffset.UTC),
            positionSource = FakeCelestialPositionSource(),
            viewport = FULL_VIEWPORT
        ).render()

        assertTrue(bitmap.hasWarmPixelNear(225, 65))
    }

    @Test
    fun celestialOverlayDoesNotDrawCompassLetters() {
        val bitmap = CelestialOverlayRenderer(
            clock = Clock.fixed(Instant.parse("2026-07-07T10:00:00Z"), ZoneOffset.UTC),
            positionSource = FakeCelestialPositionSource(positions = emptyList()),
            viewport = FULL_VIEWPORT
        ).render()

        assertFalse(bitmap.hasBrightPixelInCompassLabelAnnulus())
    }

    @Test
    fun celestialOverlayDrawsHorizonMarkerOnBodyOrbit() {
        val markerTime = Instant.parse("2026-07-07T10:00:00Z")
        val bitmap = CelestialHorizonOverlayRenderer(
            clock = Clock.fixed(markerTime, ZoneOffset.UTC),
            horizonSource = FakeCelestialHorizonSource(
                markers = listOf(
                    CelestialHorizonMarker(
                        body = CelestialBody.MARS,
                        type = CelestialHorizonEventType.RISE,
                        time = markerTime,
                        azimuthDegrees = 180.0
                    )
                )
            ),
            viewport = FULL_VIEWPORT
        ).render()

        assertTrue(bitmap.hasRedPixelNear(225, 95))
    }

    @Test
    fun constellationBackgroundExtendsAcrossFullWatchRadius() {
        val bitmap = Bitmap.createBitmap(
            DialGeometry.canvasSize,
            DialGeometry.canvasSize,
            Bitmap.Config.ARGB_8888
        )
        ConstellationBackgroundRenderer().render(
            Canvas(bitmap),
            Paint(Paint.ANTI_ALIAS_FLAG),
            listOf(
                ConstellationLine(
                    constellationId = "Test",
                    from = SkyPoint(azimuthDegrees = 180.0, zenithDistanceDegrees = 0.0),
                    to = SkyPoint(azimuthDegrees = 180.0, zenithDistanceDegrees = 90.0)
                )
            )
        )

        assertTrue(bitmap.hasConstellationPixelInsideCentralRadius())
        assertTrue(bitmap.hasConstellationPixelOutsideCentralRadius())
        assertFalse(bitmap.hasConstellationPixelOutsideWatchRadius())
    }

    @Test
    fun constellationBackgroundUsesSkyMapOrientation() {
        val bitmap = Bitmap.createBitmap(
            DialGeometry.canvasSize,
            DialGeometry.canvasSize,
            Bitmap.Config.ARGB_8888
        )
        ConstellationBackgroundRenderer().render(
            Canvas(bitmap),
            Paint(Paint.ANTI_ALIAS_FLAG),
            listOf(
                ConstellationLine(
                    constellationId = "North",
                    from = SkyPoint(azimuthDegrees = 0.0, zenithDistanceDegrees = 0.0),
                    to = SkyPoint(azimuthDegrees = 0.0, zenithDistanceDegrees = 20.0)
                ),
                ConstellationLine(
                    constellationId = "West",
                    from = SkyPoint(azimuthDegrees = 270.0, zenithDistanceDegrees = 0.0),
                    to = SkyPoint(azimuthDegrees = 270.0, zenithDistanceDegrees = 20.0)
                )
            )
        )

        assertTrue(bitmap.hasConstellationPixelNear(225, 175))
        assertTrue(bitmap.hasConstellationPixelNear(275, 225))
    }

    @Test
    fun statusOverlayRendersCentralInformation() {
        val bitmap = StatusOverlayRenderer(
            clock = Clock.fixed(Instant.parse("2026-07-07T10:00:00Z"), ZoneOffset.UTC),
            statusSource = FakeWatchStatusSource(),
            viewport = FULL_VIEWPORT
        ).render()

        assertTrue(bitmap.hasBrightPixelNear(225, 155))
        assertTrue(bitmap.hasBrightPixelNear(225, 295))
    }

    @Test
    fun statusOverlayMoonPhaseShadowTracksCardinalPhases() {
        val newMoon = renderStatusOverlay(phaseAngleDegrees = 0.0)
        val firstQuarter = renderStatusOverlay(phaseAngleDegrees = 90.0)
        val fullMoon = renderStatusOverlay(phaseAngleDegrees = 180.0)
        val lastQuarter = renderStatusOverlay(phaseAngleDegrees = 270.0)

        assertTrue(newMoon.hasMoonShadowPixelNear(225, 155))
        assertTrue(firstQuarter.hasMoonShadowPixelNear(213, 155))
        assertTrue(firstQuarter.hasMoonLitPixelNear(237, 155))
        assertTrue(fullMoon.hasMoonLitPixelNear(225, 155))
        assertTrue(lastQuarter.hasMoonLitPixelNear(213, 155))
        assertTrue(lastQuarter.hasMoonShadowPixelNear(237, 155))
    }

    @Test
    fun statusOverlayFullMoonIncludesSurfaceTexture() {
        val bitmap = renderStatusOverlay(phaseAngleDegrees = 180.0)

        assertTrue(bitmap.hasFullMoonTextureVariation())
    }

    @Test
    fun modeOverlayFullDialIsTransparent() {
        val bitmap = ModeOverlayRenderer(
            mode = DisplayMode.FULL_DIAL,
            clock = Clock.fixed(Instant.parse("2026-07-07T10:00:00Z"), ZoneOffset.UTC),
            constellationSource = FakeConstellationSource(),
            solarSystemPositionSource = FakeSolarSystemPositionSource()
        ).render()

        assertTrue(bitmap.isFullyTransparent())
    }

    @Test
    fun modeOverlayConstellationsNightDrawsOnlyOpaqueRedConstellationsOnBlack() {
        val bitmap = ModeOverlayRenderer(
            mode = DisplayMode.CONSTELLATIONS_NIGHT,
            clock = Clock.fixed(Instant.parse("2026-07-07T10:00:00Z"), ZoneOffset.UTC),
            constellationSource = FakeConstellationSource(
                lines = listOf(
                    ConstellationLine(
                        constellationId = "Night",
                        from = SkyPoint(azimuthDegrees = 180.0, zenithDistanceDegrees = 0.0),
                        to = SkyPoint(azimuthDegrees = 180.0, zenithDistanceDegrees = 20.0)
                    )
                )
            ),
            solarSystemPositionSource = FakeSolarSystemPositionSource()
        ).render()

        assertEquals(Color.BLACK, bitmap.getPixel(0, 0))
        assertTrue(bitmap.hasOpaqueRedPixelNear(225, 175))
        assertFalse(bitmap.hasBrightPixelNear(225, 175))
    }

    @Test
    fun modeOverlaySolarSystemDrawsSunAtCenterAndEarthOnOrbit() {
        val bitmap = ModeOverlayRenderer(
            mode = DisplayMode.SOLAR_SYSTEM,
            clock = Clock.fixed(Instant.parse("2026-07-07T10:00:00Z"), ZoneOffset.UTC),
            constellationSource = FakeConstellationSource(),
            solarSystemPositionSource = FakeSolarSystemPositionSource()
        ).render()

        assertEquals(Color.BLACK, bitmap.getPixel(0, 0))
        assertTrue(bitmap.hasWarmPixelNear(225, 225))
        assertTrue(bitmap.hasEarthPixelNear(313, 225))
        assertTrue(bitmap.hasStarPixelNear(92, 96))
        assertTrue(bitmap.hasEarthTailPixelNear(282, 158))
        assertEquals(Color.BLACK, bitmap.getPixel(137, 225))
    }

    private fun renderStatusOverlay(
        phaseAngleDegrees: Double = 180.0
    ): Bitmap =
        StatusOverlayRenderer(
            clock = Clock.fixed(Instant.parse("2026-07-07T10:00:00Z"), ZoneOffset.UTC),
            statusSource = FakeWatchStatusSource(
                phaseAngleDegrees = phaseAngleDegrees
            ),
            viewport = FULL_VIEWPORT
        ).render()

    private fun Bitmap.hasVisiblePixel(): Boolean {
        var x = 0
        while (x < width) {
            var y = 0
            while (y < height) {
                if (getPixel(x, y) ushr 24 != 0) {
                    return true
                }
                y += 5
            }
            x += 5
        }
        return false
    }

    private fun Bitmap.isFullyTransparent(): Boolean {
        var x = 0
        while (x < width) {
            var y = 0
            while (y < height) {
                if (Color.alpha(getPixel(x, y)) != 0) {
                    return false
                }
                y += 1
            }
            x += 1
        }
        return true
    }

    private fun Bitmap.hasWarmAstroArcPixel(): Boolean {
        var x = 0
        while (x < width) {
            var y = 0
            while (y < height) {
                val pixel = getPixel(x, y)
                if (
                    Color.alpha(pixel) > 0 &&
                    Color.red(pixel) > 220 &&
                    Color.green(pixel) in 140..220 &&
                    Color.blue(pixel) < 120
                ) {
                    return true
                }
                y += 1
            }
            x += 1
        }
        return false
    }

    private fun Bitmap.hasWarmPixelNear(centerX: Int, centerY: Int): Boolean {
        var x = centerX - 8
        while (x <= centerX + 8) {
            var y = centerY - 8
            while (y <= centerY + 8) {
                val pixel = getPixel(x, y)
                if (
                    Color.alpha(pixel) > 0 &&
                    Color.red(pixel) > 220 &&
                    Color.green(pixel) > 170 &&
                    Color.blue(pixel) < 140
                ) {
                    return true
                }
                y += 1
            }
            x += 1
        }
        return false
    }

    private fun Bitmap.hasConstellationPixelInsideCentralRadius(): Boolean {
        var x = 0
        while (x < width) {
            var y = 0
            while (y < height) {
                if (isInsideCentralRadius(x, y) && getPixel(x, y).isConstellationPixel()) {
                    return true
                }
                y += 1
            }
            x += 1
        }
        return false
    }

    private fun Bitmap.hasConstellationPixelOutsideCentralRadius(): Boolean {
        var x = 0
        while (x < width) {
            var y = 0
            while (y < height) {
                if (!isInsideCentralRadius(x, y) && getPixel(x, y).isConstellationPixel()) {
                    return true
                }
                y += 1
            }
            x += 1
        }
        return false
    }

    private fun Bitmap.hasConstellationPixelOutsideWatchRadius(): Boolean {
        var x = 0
        while (x < width) {
            var y = 0
            while (y < height) {
                if (!isInsideWatchRadius(x, y) && getPixel(x, y).isConstellationPixel()) {
                    return true
                }
                y += 1
            }
            x += 1
        }
        return false
    }

    private fun Bitmap.hasConstellationPixelNear(centerX: Int, centerY: Int): Boolean {
        var x = centerX - 3
        while (x <= centerX + 3) {
            var y = centerY - 3
            while (y <= centerY + 3) {
                if (getPixel(x, y).isConstellationPixel()) {
                    return true
                }
                y += 1
            }
            x += 1
        }
        return false
    }

    private fun Bitmap.hasBrightPixelNear(centerX: Int, centerY: Int): Boolean {
        var x = centerX - 16
        while (x <= centerX + 16) {
            var y = centerY - 12
            while (y <= centerY + 12) {
                if (getPixel(x, y).isBrightPixel()) {
                    return true
                }
                y += 1
            }
            x += 1
        }
        return false
    }

    private fun Bitmap.hasBrightPixelInCompassLabelAnnulus(): Boolean {
        var x = 0
        while (x < width) {
            var y = 0
            while (y < height) {
                val dx = x - 225
                val dy = y - 225
                val radiusSquared = dx * dx + dy * dy
                if (
                    radiusSquared in 150 * 150..162 * 162 &&
                    getPixel(x, y).isBrightPixel()
                ) {
                    return true
                }
                y += 1
            }
            x += 1
        }
        return false
    }

    private fun Bitmap.hasMoonLitPixelNear(centerX: Int, centerY: Int): Boolean {
        var x = centerX - 3
        while (x <= centerX + 3) {
            var y = centerY - 3
            while (y <= centerY + 3) {
                if (getPixel(x, y).isMoonLitPixel()) {
                    return true
                }
                y += 1
            }
            x += 1
        }
        return false
    }

    private fun Bitmap.hasMoonShadowPixelNear(centerX: Int, centerY: Int): Boolean {
        var x = centerX - 3
        while (x <= centerX + 3) {
            var y = centerY - 3
            while (y <= centerY + 3) {
                if (getPixel(x, y).isMoonShadowPixel()) {
                    return true
                }
                y += 1
            }
            x += 1
        }
        return false
    }

    private fun Bitmap.hasFullMoonTextureVariation(): Boolean {
        val luminanceBuckets = mutableSetOf<Int>()
        var x = 201
        while (x <= 249) {
            var y = 131
            while (y <= 179) {
                if (isInsideMoon(x, y)) {
                    val pixel = getPixel(x, y)
                    if (Color.alpha(pixel) > 0) {
                        luminanceBuckets += pixel.moonLuminance() / 12
                    }
                }
                y += 1
            }
            x += 1
        }
        return luminanceBuckets.size >= 5
    }

    private fun Bitmap.hasGreenPixelNear(centerX: Int, centerY: Int): Boolean {
        var x = centerX - 20
        while (x <= centerX + 46) {
            var y = centerY - 12
            while (y <= centerY + 12) {
                val pixel = getPixel(x, y)
                if (
                    Color.alpha(pixel) > 0 &&
                    Color.red(pixel) < 120 &&
                    Color.green(pixel) > 170 &&
                    Color.blue(pixel) in 70..160
                ) {
                    return true
                }
                y += 1
            }
            x += 1
        }
        return false
    }

    private fun Bitmap.hasOrangePixelNear(centerX: Int, centerY: Int): Boolean {
        var x = centerX - 20
        while (x <= centerX + 46) {
            var y = centerY - 12
            while (y <= centerY + 12) {
                val pixel = getPixel(x, y)
                if (
                    Color.alpha(pixel) > 0 &&
                    Color.red(pixel) > 220 &&
                    Color.green(pixel) in 110..180 &&
                    Color.blue(pixel) < 90
                ) {
                    return true
                }
                y += 1
            }
            x += 1
        }
        return false
    }

    private fun Bitmap.hasRedPixelNear(centerX: Int, centerY: Int): Boolean {
        var x = centerX - 20
        while (x <= centerX + 46) {
            var y = centerY - 12
            while (y <= centerY + 12) {
                val pixel = getPixel(x, y)
                if (
                    Color.alpha(pixel) > 0 &&
                    Color.red(pixel) > 190 &&
                    Color.green(pixel) < 100 &&
                    Color.blue(pixel) < 100
                ) {
                    return true
                }
                y += 1
            }
            x += 1
        }
        return false
    }

    private fun Bitmap.hasOpaqueRedPixelNear(centerX: Int, centerY: Int): Boolean {
        var x = centerX - 5
        while (x <= centerX + 5) {
            var y = centerY - 5
            while (y <= centerY + 5) {
                val pixel = getPixel(x, y)
                if (
                    Color.alpha(pixel) == 255 &&
                    Color.red(pixel) == 255 &&
                    Color.green(pixel) == 0 &&
                    Color.blue(pixel) == 0
                ) {
                    return true
                }
                y += 1
            }
            x += 1
        }
        return false
    }

    private fun Bitmap.hasEarthPixelNear(centerX: Int, centerY: Int): Boolean {
        var x = centerX - 8
        while (x <= centerX + 8) {
            var y = centerY - 8
            while (y <= centerY + 8) {
                val pixel = getPixel(x, y)
                if (
                    Color.alpha(pixel) > 0 &&
                    Color.blue(pixel) > 170 &&
                    Color.red(pixel) < 120 &&
                    Color.green(pixel) > 90
                ) {
                    return true
                }
                y += 1
            }
            x += 1
        }
        return false
    }

    private fun Bitmap.hasEarthTailPixelNear(centerX: Int, centerY: Int): Boolean {
        var x = centerX - 8
        while (x <= centerX + 8) {
            var y = centerY - 8
            while (y <= centerY + 8) {
                val pixel = getPixel(x, y)
                if (
                    Color.alpha(pixel) > 0 &&
                    Color.blue(pixel) > 45 &&
                    Color.blue(pixel) > Color.green(pixel) &&
                    Color.green(pixel) > Color.red(pixel)
                ) {
                    return true
                }
                y += 1
            }
            x += 1
        }
        return false
    }

    private fun Bitmap.hasStarPixelNear(centerX: Int, centerY: Int): Boolean {
        var x = centerX - 3
        while (x <= centerX + 3) {
            var y = centerY - 3
            while (y <= centerY + 3) {
                val pixel = getPixel(x, y)
                if (
                    Color.alpha(pixel) > 0 &&
                    Color.red(pixel) > 120 &&
                    Color.green(pixel) > 120 &&
                    Color.blue(pixel) > 120
                ) {
                    return true
                }
                y += 1
            }
            x += 1
        }
        return false
    }

    private fun Int.isBrightPixel(): Boolean =
        Color.alpha(this) > 0 &&
            Color.red(this) > 190 &&
            Color.green(this) > 190 &&
            Color.blue(this) > 185

    private fun Int.isMoonLitPixel(): Boolean =
        Color.alpha(this) > 0 &&
            Color.red(this) > 115 &&
            Color.green(this) > 115 &&
            Color.blue(this) > 110

    private fun Int.isMoonShadowPixel(): Boolean =
        Color.alpha(this) > 0 &&
            Color.red(this) < 85 &&
            Color.green(this) < 90 &&
            Color.blue(this) < 95

    private fun Int.moonLuminance(): Int =
        (Color.red(this) * 299 + Color.green(this) * 587 + Color.blue(this) * 114) / 1000

    private fun isInsideCentralRadius(x: Int, y: Int): Boolean {
        val dx = x - 225
        val dy = y - 225
        return dx * dx + dy * dy <= 110 * 110
    }

    private fun isInsideWatchRadius(x: Int, y: Int): Boolean {
        val dx = x - 225
        val dy = y - 225
        return dx * dx + dy * dy <= 226 * 226
    }

    private fun isInsideMoon(x: Int, y: Int): Boolean {
        val dx = x - 225
        val dy = y - 155
        return dx * dx + dy * dy <= 23 * 23
    }

    private fun Int.isConstellationPixel(): Boolean =
        Color.alpha(this) > 0 &&
            Color.red(this) > 120 &&
            Color.green(this) < 100 &&
            Color.blue(this) < 100

    private class FakeAstroEventSource : AstroEventSource {
        override fun eventsBetween(
            start: Instant,
            end: Instant,
            observer: AstroObserver
        ): List<AstroEvent> = emptyList()

        override fun sunAltitudeDegrees(time: Instant, observer: AstroObserver): Double = 12.0

        override fun moonAltitudeDegrees(time: Instant, observer: AstroObserver): Double = -2.0
    }

    private class CountingRenderer : DialRenderer {
        override val contentDescription = "Counting renderer"
        var renderCount = 0
            private set

        override fun renderAt(instant: Instant): Bitmap {
            renderCount += 1
            return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        }

        override fun render(): Bitmap = renderAt(Instant.EPOCH)
    }

    private class ValidityRenderer(
        private val validity: Duration
    ) : DialRenderer {
        override val contentDescription = "Validity renderer"

        override fun renderAt(instant: Instant): Bitmap =
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

        override fun renderFrameAt(instant: Instant): RenderedDialFrame =
            RenderedDialFrame(
                bitmap = renderAt(instant),
                contentKey = instant,
                validUntil = instant.plus(validity)
            )

        override fun render(): Bitmap = renderAt(Instant.EPOCH)
    }

    private class FakeCelestialPositionSource(
        private val positions: List<CelestialPosition> = listOf(
            CelestialPosition(CelestialBody.SUN, 180.0),
            CelestialPosition(CelestialBody.MOON, 270.0),
            CelestialPosition(CelestialBody.MARS, 0.0),
            CelestialPosition(CelestialBody.NEPTUNE, 90.0)
        )
    ) : CelestialPositionSource {
        override fun positionsAt(
            time: Instant,
            observer: AstroObserver
        ): CelestialPositionSnapshot =
            CelestialPositionSnapshot(
                calculatedAt = time,
                positions = positions
            )
    }

    private class FakeCelestialHorizonSource(
        private val markers: List<CelestialHorizonMarker> = emptyList()
    ) : CelestialHorizonSource {
        override fun horizonMarkersAt(
            time: Instant,
            observer: AstroObserver
        ): CelestialHorizonSnapshot =
            CelestialHorizonSnapshot(
                calculatedAt = time,
                localDate = time.atZone(observer.zoneId).toLocalDate(),
                markers = markers
            )
    }

    private class FakeConstellationSource(
        private val lines: List<ConstellationLine> = listOf(
            ConstellationLine(
                constellationId = "Test",
                from = SkyPoint(azimuthDegrees = 180.0, zenithDistanceDegrees = 4.0),
                to = SkyPoint(azimuthDegrees = 270.0, zenithDistanceDegrees = 12.0)
            )
        )
    ) : ConstellationSource {
        override fun constellationsAt(
            time: Instant,
            observer: AstroObserver
        ): ConstellationSnapshot =
            ConstellationSnapshot(
                calculatedAt = time,
                refreshInstant = Instant.parse("2026-07-07T04:00:00Z"),
                targetMidnight = Instant.parse("2026-07-07T22:00:00Z"),
                nextRefreshInstant = Instant.parse("2026-07-08T04:00:00Z"),
                lines = lines
            )
    }

    private class FakeWatchStatusSource(
        private val phaseAngleDegrees: Double = 180.0
    ) : WatchStatusSource {
        override fun statusAt(time: Instant): WatchStatus =
            WatchStatus(
                dateLabel = "sam. 04 juil.",
                moonPhase = MoonPhaseSnapshot(
                    calculatedAt = time,
                    targetTime = time.plusSeconds(3600),
                    phaseAngleDegrees = phaseAngleDegrees,
                    illuminationPercent = 100,
                    validUntil = time.plusSeconds(7200)
                )
            )
    }

    private companion object {
        val FULL_VIEWPORT = DialViewport(0, 0, DialGeometry.canvasSize, DialGeometry.canvasSize)
    }

    private class FakeSolarSystemPositionSource : SolarSystemPositionSource {
        override fun positionsAt(time: Instant): SolarSystemSnapshot =
            SolarSystemSnapshot(
                calculatedAt = time,
                positions = listOf(
                    SolarSystemPosition(SolarSystemBody.SUN, 0.0, 0.0),
                    SolarSystemPosition(SolarSystemBody.MERCURY, 0.0, 0.4),
                    SolarSystemPosition(SolarSystemBody.VENUS, 0.0, 0.7),
                    SolarSystemPosition(SolarSystemBody.EARTH, 90.0, 1.0),
                    SolarSystemPosition(SolarSystemBody.MARS, 180.0, 1.5),
                    SolarSystemPosition(SolarSystemBody.JUPITER, 220.0, 5.2),
                    SolarSystemPosition(SolarSystemBody.SATURN, 260.0, 9.5),
                    SolarSystemPosition(SolarSystemBody.URANUS, 300.0, 19.2),
                    SolarSystemPosition(SolarSystemBody.NEPTUNE, 340.0, 30.1)
                )
            )
    }
}
