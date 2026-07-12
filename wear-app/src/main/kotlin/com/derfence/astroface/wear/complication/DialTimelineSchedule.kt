package com.derfence.astroface.wear.complication

import androidx.wear.watchface.complications.datasource.TimeInterval
import java.time.Duration
import java.time.Instant

data class DialTimelineSchedule(
    val cadence: Duration,
    val horizon: Duration
) {
    init {
        require(!cadence.isZero && !cadence.isNegative) { "Cadence must be positive." }
        require(!horizon.isZero && !horizon.isNegative) { "Horizon must be positive." }
        require(horizon.toMillis() % cadence.toMillis() == 0L) {
            "Horizon must be an exact multiple of cadence."
        }
    }

    fun intervalsStartingAt(start: Instant): List<TimeInterval> {
        val count = (horizon.toMillis() / cadence.toMillis()).toInt()
        return (0 until count).map { index ->
            val intervalStart = start.plus(cadence.multipliedBy(index.toLong()))
            TimeInterval(intervalStart, intervalStart.plus(cadence))
        }
    }

    companion object {
        val WatchMode = DialTimelineSchedule(
            cadence = Duration.ofMinutes(10),
            horizon = Duration.ofHours(2)
        )
        val PassageMode = DialTimelineSchedule(
            cadence = Duration.ofHours(24),
            horizon = Duration.ofHours(48)
        )
    }
}
