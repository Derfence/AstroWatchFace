package com.derfence.astroface.wear.complication

import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationDataTimeline
import androidx.wear.watchface.complications.datasource.TimeInterval
import androidx.wear.watchface.complications.datasource.TimelineEntry
import com.derfence.astroface.wear.astro.AstroObserver
import java.time.Instant

object Dial24hComplicationDataFactory {
    private const val CONTENT_DESCRIPTION = "Cadran AstroFace 24 heures"

    fun createTimeline(
        start: Instant,
        planner: Dial24hTimelinePlanner = Dial24hTimelinePlanner()
    ): ComplicationDataTimeline {
        val dataByFields = mutableMapOf<Dial24hFields, RangedValueComplicationData>()
        val entries = planner.plan(start).map { entry ->
            TimelineEntry(
                TimeInterval(entry.start, entry.end),
                dataByFields.getOrPut(entry.fields) { create(entry.fields) }
            )
        }
        check(entries.isNotEmpty()) { "Dial 24 h timeline must contain data." }
        return ComplicationDataTimeline(
            defaultComplicationData = entries.first().complicationData,
            timelineEntries = entries
        )
    }

    fun createCurrent(
        instant: Instant,
        observer: AstroObserver = AstroObserver.DEFAULT,
        projectionBuilder: Dial24hProjectionBuilder = Dial24hProjectionBuilder()
    ): RangedValueComplicationData =
        create(Dial24hCodec.fieldsFor(projectionBuilder.projectionAt(instant, observer)))

    private fun create(fields: Dial24hFields): RangedValueComplicationData {
        val text = PlainComplicationText.Builder(CONTENT_DESCRIPTION).build()
        return RangedValueComplicationData.Builder(
            fields.value,
            fields.minimum,
            fields.maximum,
            text
        ).setText(text).build()
    }
}
