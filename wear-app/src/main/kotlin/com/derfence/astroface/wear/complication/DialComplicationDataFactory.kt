package com.derfence.astroface.wear.complication

import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.PhotoImageComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.datasource.ComplicationDataTimeline
import androidx.wear.watchface.complications.datasource.TimeInterval
import androidx.wear.watchface.complications.datasource.TimelineEntry
import com.derfence.astroface.wear.dial.DialRenderer
import com.derfence.astroface.wear.dial.RenderedDialFrame
import java.time.Instant

object DialComplicationDataFactory {
    fun create(renderer: DialRenderer, instant: Instant): PhotoImageComplicationData =
        create(renderer.renderFrameAt(instant), renderer.contentDescription)

    private fun create(
        frame: RenderedDialFrame,
        contentDescription: String
    ): PhotoImageComplicationData =
        PhotoImageComplicationData.Builder(
            Icon.createWithBitmap(frame.bitmap),
            PlainComplicationText.Builder(contentDescription).build()
        ).build()

    fun createTimeline(
        renderer: DialRenderer,
        start: Instant,
        schedule: DialTimelineSchedule
    ): ComplicationDataTimeline {
        val framesByKey = mutableMapOf<Any, PhotoImageComplicationData>()
        val entries = schedule.intervalsStartingAt(start).map { interval ->
            val frame = renderer.renderFrameAt(interval.start)
            TimelineEntry(
                interval,
                framesByKey.getOrPut(frame.contentKey) {
                    create(frame, renderer.contentDescription)
                }
            )
        }
        val defaultData = entries.firstOrNull()?.complicationData
            ?: create(renderer, start)
        return ComplicationDataTimeline(
            defaultComplicationData = defaultData,
            timelineEntries = entries
        )
    }

    fun createTimeline(
        renderer: DialRenderer,
        start: Instant,
        plan: DialTimelinePlan
    ): ComplicationDataTimeline =
        when (plan) {
            is DialTimelinePlan.Fixed -> createTimeline(renderer, start, plan.schedule)
            is DialTimelinePlan.ByValidity -> createValidityTimeline(renderer, start, plan)
        }

    private fun createValidityTimeline(
        renderer: DialRenderer,
        start: Instant,
        plan: DialTimelinePlan.ByValidity
    ): ComplicationDataTimeline {
        val timelineEnd = start.plus(plan.horizon)
        val framesByKey = mutableMapOf<Any, PhotoImageComplicationData>()
        val entries = mutableListOf<TimelineEntry>()
        var cursor = start

        while (cursor.isBefore(timelineEnd)) {
            check(entries.size < plan.maxEntries) {
                "Renderer validity produced more than ${plan.maxEntries} timeline entries."
            }
            val frame = renderer.renderFrameAt(cursor)
            val frameEnd = frame.validUntil
                ?.takeIf { it.isAfter(cursor) }
                ?.let { minOf(it, timelineEnd) }
                ?: timelineEnd
            val data = framesByKey.getOrPut(frame.contentKey) {
                create(frame, renderer.contentDescription)
            }
            entries += TimelineEntry(TimeInterval(cursor, frameEnd), data)
            cursor = frameEnd
        }

        return ComplicationDataTimeline(
            defaultComplicationData = entries.first().complicationData,
            timelineEntries = entries
        )
    }
}
