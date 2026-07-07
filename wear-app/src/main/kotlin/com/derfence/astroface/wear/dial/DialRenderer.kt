package com.derfence.astroface.wear.dial

import android.graphics.Bitmap

interface DialRenderer {
    val contentDescription: String

    fun render(): Bitmap
}
