package com.derfence.astroface.wear.dial

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.derfence.astroface.wear.R

internal object MoonSurfaceTexture {
    fun decode(resources: Resources): Bitmap =
        requireNotNull(
            BitmapFactory.decodeResource(
                resources,
                R.drawable.moon_surface,
                BitmapFactory.Options().apply {
                    inScaled = false
                    inSampleSize = SOURCE_SAMPLE_SIZE
                }
            )
        ) { "Unable to decode the Moon surface texture" }

    private const val SOURCE_SAMPLE_SIZE = 4
}
