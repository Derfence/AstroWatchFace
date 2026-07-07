package com.derfence.astroface.wear.complication

import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.PhotoImageComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import com.derfence.astroface.wear.dial.DialRenderer

object DialComplicationDataFactory {
    fun create(renderer: DialRenderer): PhotoImageComplicationData =
        PhotoImageComplicationData.Builder(
            Icon.createWithBitmap(renderer.render()),
            PlainComplicationText.Builder(renderer.contentDescription).build()
        ).build()
}
