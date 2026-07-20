package com.derfence.astroface.wear.complication

import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationDataTimeline
import androidx.wear.watchface.complications.datasource.TimeInterval
import androidx.wear.watchface.complications.datasource.TimelineEntry
import com.derfence.astroface.wear.astro.AstroObserver
import com.derfence.astroface.wear.astro.CelestialPositionSource
import com.derfence.astroface.wear.astro.SharedAstronomySources
import java.time.Instant
import java.time.ZoneId

object CelestialMotionComplicationDataFactory {
    fun createTimeline(
        group: CelestialMotionGroup,
        start: Instant,
        observer: AstroObserver = AstroObserver.DEFAULT,
        zoneId: ZoneId = ZoneId.systemDefault(),
        positionSource: CelestialPositionSource = SharedAstronomySources.celestialPositionSource
    ): ComplicationDataTimeline {
        val entries = planner(positionSource, observer, zoneId)
            .plan(start, group)
            .map { entry ->
                TimelineEntry(
                    TimeInterval(entry.start, entry.end),
                    create(entry.fields, group.contentDescription)
                )
            }
        check(entries.isNotEmpty()) { "Celestial motion timeline must contain data." }
        return ComplicationDataTimeline(
            defaultComplicationData = entries.first().complicationData,
            timelineEntries = entries
        )
    }

    fun createCurrent(
        group: CelestialMotionGroup,
        instant: Instant,
        observer: AstroObserver = AstroObserver.DEFAULT,
        zoneId: ZoneId = ZoneId.systemDefault(),
        positionSource: CelestialPositionSource = SharedAstronomySources.celestialPositionSource
    ): RangedValueComplicationData {
        val entry = planner(positionSource, observer, zoneId).plan(instant, group).first()
        return create(entry.fields, group.contentDescription)
    }

    private fun create(
        fields: CelestialMotionFields,
        description: String
    ): RangedValueComplicationData {
        val text = PlainComplicationText.Builder(description).build()
        return RangedValueComplicationData.Builder(
            fields.value,
            fields.minimum,
            fields.maximum,
            text
        ).setText(text).build()
    }

    private fun planner(
        positionSource: CelestialPositionSource,
        observer: AstroObserver,
        zoneId: ZoneId
    ) = CelestialMotionTimelinePlanner(positionSource, observer, zoneId)
}
