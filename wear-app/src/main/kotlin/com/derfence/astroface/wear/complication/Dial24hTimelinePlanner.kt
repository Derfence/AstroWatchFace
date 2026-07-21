package com.derfence.astroface.wear.complication

import com.derfence.astroface.wear.astro.AstroEventSource
import com.derfence.astroface.wear.astro.AstroObserver
import com.derfence.astroface.wear.astro.SharedAstronomySources
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

data class Dial24hTimelineEntry(
    val start: Instant,
    val end: Instant,
    val fields: Dial24hFields
)

class Dial24hTimelinePlanner(
    private val eventSource: AstroEventSource = SharedAstronomySources.astroEventSource,
    private val projectionBuilder: Dial24hProjectionBuilder = Dial24hProjectionBuilder(eventSource),
    private val observer: AstroObserver = AstroObserver.DEFAULT,
    private val horizon: Duration = HORIZON
) {
    init {
        require(!horizon.isZero && !horizon.isNegative) { "Horizon must be positive." }
    }

    fun plan(requestInstant: Instant): List<Dial24hTimelineEntry> {
        val timelineEnd = requestInstant.plus(horizon)
        val searchEnd = timelineEnd
            .plus(ROLLING_WINDOW)
            .plus(Dial24hProjectionBuilder.SOLAR_JOIN_TOLERANCE)
        val changes = sortedSetOf(requestInstant, timelineEnd)

        eventSource.eventsBetween(requestInstant, searchEnd, observer).forEach { event ->
            addBoundary(changes, ceilToMinute(event.time), requestInstant, timelineEnd)
            addBoundary(
                changes,
                ceilToMinute(event.time.minus(ROLLING_WINDOW)),
                requestInstant,
                timelineEnd
            )
        }

        return changes.zipWithNext().map { (start, end) ->
            Dial24hTimelineEntry(
                start = start,
                end = end,
                fields = Dial24hCodec.fieldsFor(projectionBuilder.projectionAt(start, observer))
            )
        }
    }

    private fun addBoundary(
        changes: MutableSet<Instant>,
        candidate: Instant,
        start: Instant,
        end: Instant
    ) {
        if (candidate.isAfter(start) && candidate.isBefore(end)) {
            changes += candidate
        }
    }

    private fun ceilToMinute(instant: Instant): Instant {
        val truncated = instant.truncatedTo(ChronoUnit.MINUTES)
        return if (truncated == instant) truncated else truncated.plus(1, ChronoUnit.MINUTES)
    }

    companion object {
        val HORIZON: Duration = Duration.ofHours(10)
        val ROLLING_WINDOW: Duration = Duration.ofHours(24)
    }
}
