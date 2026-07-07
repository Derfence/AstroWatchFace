package com.derfence.astroface.wear.dial

import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.wear.watchface.complications.data.ComplicationType
import com.derfence.astroface.wear.astro.AstroEvent
import com.derfence.astroface.wear.astro.AstroEventSource
import com.derfence.astroface.wear.astro.AstroObserver
import com.derfence.astroface.wear.astro.AstroWindowCalculator
import com.derfence.astroface.wear.complication.DialComplicationDataFactory
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DialRenderInstrumentedTest {
    @Test
    fun renderersProduceVisiblePixels() {
        assertTrue(Dial24hRenderer().render().hasVisiblePixel())
        assertTrue(
            Hour24hHandRenderer(
                clock = Clock.fixed(Instant.parse("2026-07-07T10:00:00Z"), ZoneOffset.UTC)
            ).render().hasVisiblePixel()
        )
    }

    @Test
    fun complicationFactoryReturnsPhotoImageData() {
        val data = DialComplicationDataFactory.create(Dial24hRenderer())

        assertEquals(ComplicationType.PHOTO_IMAGE, data.type)
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

    private class FakeAstroEventSource : AstroEventSource {
        override fun eventsBetween(
            start: Instant,
            end: Instant,
            observer: AstroObserver
        ): List<AstroEvent> = emptyList()

        override fun sunAltitudeDegrees(time: Instant, observer: AstroObserver): Double = 12.0

        override fun moonAltitudeDegrees(time: Instant, observer: AstroObserver): Double = -2.0
    }
}
