package com.derfence.astroface.wear.complication

import java.time.Duration

sealed interface DialTimelinePlan {
    data class Fixed(val schedule: DialTimelineSchedule) : DialTimelinePlan

    data class ByValidity(
        val horizon: Duration,
        val maxEntries: Int = DEFAULT_MAX_ENTRIES
    ) : DialTimelinePlan {
        init {
            require(!horizon.isZero && !horizon.isNegative) { "Horizon must be positive." }
            require(maxEntries > 0) { "maxEntries must be positive." }
        }
    }

    companion object {
        private const val DEFAULT_MAX_ENTRIES = 8
    }
}
