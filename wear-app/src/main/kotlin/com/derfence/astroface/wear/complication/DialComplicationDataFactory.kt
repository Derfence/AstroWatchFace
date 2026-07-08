package com.derfence.astroface.wear.complication

import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.PhotoImageComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.TimeRange
import com.derfence.astroface.wear.dial.DialRenderer

object DialComplicationDataFactory {
    fun create(renderer: DialRenderer): PhotoImageComplicationData {
        val builder = PhotoImageComplicationData.Builder(
            Icon.createWithBitmap(renderer.render()),
            PlainComplicationText.Builder(renderer.contentDescription).build()
        )

        renderer.validUntil?.let { builder.setValidTimeRange(TimeRange.before(it)) }

        return builder.build()
    }
}
