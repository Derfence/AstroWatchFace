package com.derfence.astroface.wear.complication

import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.PhotoImageComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.datasource.ComplicationDataTimeline
import androidx.wear.watchface.complications.datasource.TimelineEntry
import com.derfence.astroface.wear.dial.DialRenderer
import java.time.Instant

object DialComplicationDataFactory {
    fun create(renderer: DialRenderer, instant: Instant): PhotoImageComplicationData =
        PhotoImageComplicationData.Builder(
            Icon.createWithBitmap(renderer.renderAt(instant)),
            PlainComplicationText.Builder(renderer.contentDescription).build()
        ).build()

    fun createTimeline(
        renderer: DialRenderer,
        start: Instant,
        schedule: DialTimelineSchedule
    ): ComplicationDataTimeline {
        val entries = schedule.intervalsStartingAt(start).map { interval ->
            TimelineEntry(
                interval,
                create(renderer, interval.start)
            )
        }
        return ComplicationDataTimeline(
            defaultComplicationData = create(renderer, start),
            timelineEntries = entries
        )
    }
}
