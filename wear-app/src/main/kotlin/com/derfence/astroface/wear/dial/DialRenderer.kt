package com.derfence.astroface.wear.dial

import android.graphics.Bitmap
import java.time.Instant

interface DialRenderer {
    val contentDescription: String

    fun renderAt(instant: Instant): Bitmap
    fun render(): Bitmap
}
