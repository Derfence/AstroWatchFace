package com.derfence.astroface.wear.dial

import android.graphics.Bitmap
import java.time.Instant

interface DialRenderer {
    val contentDescription: String
    val validUntil: Instant?
        get() = null

    fun render(): Bitmap
}
