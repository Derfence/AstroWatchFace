package com.derfence.astroface.wear.dial

import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.wear.watchface.complications.data.ComplicationType
import com.derfence.astroface.wear.complication.DialComplicationDataFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DialRenderInstrumentedTest {
    @Test
    fun renderersProduceVisiblePixels() {
        assertTrue(Dial24hRenderer().render().hasVisiblePixel())
        assertTrue(Dial12hRenderer().render().hasVisiblePixel())
    }

    @Test
    fun complicationFactoryReturnsPhotoImageData() {
        val data = DialComplicationDataFactory.create(Dial24hRenderer())

        assertEquals(ComplicationType.PHOTO_IMAGE, data.type)
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
}
