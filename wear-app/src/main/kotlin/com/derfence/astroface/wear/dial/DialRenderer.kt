package com.derfence.astroface.wear.dial

import android.graphics.Bitmap
import java.time.Instant

interface DialRenderer {
    val contentDescription: String

    fun renderAt(instant: Instant): Bitmap
    fun renderFrameAt(instant: Instant): RenderedDialFrame =
        RenderedDialFrame(
            bitmap = renderAt(instant),
            contentKey = instant
        )
    fun render(): Bitmap
}

data class RenderedDialFrame(
    val bitmap: Bitmap,
    val contentKey: Any,
    val validUntil: Instant? = null
)
