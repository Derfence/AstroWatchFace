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
import com.derfence.astroface.wear.astro.SkyPoint
import com.derfence.astroface.wear.complication.DialComplicationDataFactory
import com.derfence.astroface.wear.status.BatteryStatus
import com.derfence.astroface.wear.status.WatchStatus
import com.derfence.astroface.wear.status.WatchStatusSource
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DialRenderInstrumentedTest {
    @Test
    fun renderersProduceVisiblePixels() {
        assertTrue(Dial24hRenderer(constellationSource = FakeConstellationSource()).render().hasVisiblePixel())
        assertTrue(
            Hour24hHandRenderer(
                clock = Clock.fixed(Instant.parse("2026-07-07T10:00:00Z"), ZoneOffset.UTC)
            ).render().hasVisiblePixel()
        )
        assertTrue(
            CelestialOverlayRenderer(
                clock = Clock.fixed(Instant.parse("2026-07-07T10:00:00Z"), ZoneOffset.UTC),
                positionSource = FakeCelestialPositionSource(),
                horizonSource = FakeCelestialHorizonSource()
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
        val data = DialComplicationDataFactory.create(
            Dial24hRenderer(constellationSource = FakeConstellationSource())
        )

        assertEquals(ComplicationType.PHOTO_IMAGE, data.type)
    }

    @Test
    fun render24hShowsInjectedAstroArcColor() {
        val renderer = Dial24hRenderer(
            clock = Clock.fixed(Instant.parse("2026-07-07T10:00:00Z"), ZoneOffset.UTC),
            astroWindowCalculator = AstroWindowCalculator(FakeAstroEventSource()),
            constellationSource = FakeConstellationSource()
        )

        val bitmap = renderer.render()

        assertTrue(bitmap.hasWarmAstroArcPixel())
    }

    @Test
    fun celestialOverlayPlacesSouthAzimuthAtTop() {
        val bitmap = CelestialOverlayRenderer(
            clock = Clock.fixed(Instant.parse("2026-07-07T10:00:00Z"), ZoneOffset.UTC),
            positionSource = FakeCelestialPositionSource(),
            horizonSource = FakeCelestialHorizonSource()
        ).render()

        assertTrue(bitmap.hasWarmPixelNear(225, 65))
    }

    @Test
    fun celestialOverlayDrawsHorizonMarkerOnBodyOrbit() {
        val markerTime = Instant.parse("2026-07-07T10:00:00Z")
        val bitmap = CelestialOverlayRenderer(
            clock = Clock.fixed(markerTime, ZoneOffset.UTC),
            positionSource = FakeCelestialPositionSource(),
            horizonSource = FakeCelestialHorizonSource(
                markers = listOf(
                    CelestialHorizonMarker(
                        body = CelestialBody.MARS,
                        type = CelestialHorizonEventType.RISE,
                        time = markerTime,
                        azimuthDegrees = 180.0
                    )
                )
            )
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
            statusSource = FakeWatchStatusSource()
        ).render()

        assertTrue(bitmap.hasBrightPixelNear(183, 52))
        assertTrue(bitmap.hasBrightPixelNear(225, 285))
        assertTrue(bitmap.hasGreenPixelNear(267, 52))
    }

    @Test
    fun statusOverlayUsesBatteryColorThresholds() {
        val green = renderStatusOverlay(batteryPercent = 81)
        val white = renderStatusOverlay(batteryPercent = 80)
        val orange = renderStatusOverlay(batteryPercent = 25)
        val red = renderStatusOverlay(batteryPercent = 20)

        assertTrue(green.hasGreenPixelNear(267, 52))
        assertTrue(green.hasBrightPixelNear(267, 52))
        assertTrue(white.hasBrightPixelNear(267, 52))
        assertTrue(orange.hasOrangePixelNear(267, 52))
        assertTrue(orange.hasBrightPixelNear(267, 52))
        assertTrue(red.hasRedPixelNear(267, 52))
        assertTrue(red.hasBrightPixelNear(267, 52))
    }

    @Test
    fun statusOverlayBatteryFillReflectsExactPercent() {
        val bitmap = renderStatusOverlay(batteryPercent = 50)

        assertTrue(bitmap.getPixel(262, 52).isBrightPixel())
        assertEquals(0, Color.alpha(bitmap.getPixel(274, 52)))
    }

    private fun renderStatusOverlay(batteryPercent: Int): Bitmap =
        StatusOverlayRenderer(
            clock = Clock.fixed(Instant.parse("2026-07-07T10:00:00Z"), ZoneOffset.UTC),
            statusSource = FakeWatchStatusSource(batteryPercent = batteryPercent)
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

    private fun Int.isBrightPixel(): Boolean =
        Color.alpha(this) > 0 &&
            Color.red(this) > 190 &&
            Color.green(this) > 190 &&
            Color.blue(this) > 185

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

    private class FakeCelestialPositionSource : CelestialPositionSource {
        override fun positionsAt(
            time: Instant,
            observer: AstroObserver
        ): CelestialPositionSnapshot =
            CelestialPositionSnapshot(
                calculatedAt = time,
                positions = listOf(
                    CelestialPosition(CelestialBody.SUN, 180.0),
                    CelestialPosition(CelestialBody.MOON, 270.0),
                    CelestialPosition(CelestialBody.MARS, 0.0),
                    CelestialPosition(CelestialBody.NEPTUNE, 90.0)
                )
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
                lines = lines
            )
    }

    private class FakeWatchStatusSource(
        private val batteryPercent: Int = 83,
        private val phaseAngleDegrees: Double = 180.0
    ) : WatchStatusSource {
        override fun statusAt(time: Instant): WatchStatus =
            WatchStatus(
                dateLabel = "sam. 04 juil.",
                battery = BatteryStatus.fromPercent(batteryPercent),
                moonPhase = MoonPhaseSnapshot(
                    calculatedAt = time,
                    targetTime = time.plusSeconds(3600),
                    phaseAngleDegrees = phaseAngleDegrees,
                    illuminationPercent = 100,
                    validUntil = time.plusSeconds(7200)
                )
            )
    }
}
