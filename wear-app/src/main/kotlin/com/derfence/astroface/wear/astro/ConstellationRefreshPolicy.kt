package com.derfence.astroface.wear.astro

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

fun interface SunriseProvider {
    fun sunriseOn(date: LocalDate, observer: AstroObserver): Instant?
}

data class ConstellationRefreshTarget(
    val refreshInstant: Instant,
    val targetMidnight: Instant,
    val nextRefreshInstant: Instant
)

class ConstellationRefreshPolicy(
    private val sunriseProvider: SunriseProvider
) {
    fun targetFor(time: Instant, observer: AstroObserver): ConstellationRefreshTarget {
        val localNow = time.atZone(observer.zoneId)
        val today = localNow.toLocalDate()
        val todayRefresh = refreshInstantFor(today, observer)
        val refreshDate = if (!time.isBefore(todayRefresh)) {
            today
        } else {
            today.minusDays(1)
        }
        val refreshInstant = if (refreshDate == today) {
            todayRefresh
        } else {
            refreshInstantFor(refreshDate, observer)
        }
        val targetMidnight = refreshDate.plusDays(1)
            .atStartOfDay(observer.zoneId)
            .toInstant()
        val nextRefreshInstant = refreshInstantFor(refreshDate.plusDays(1), observer)

        return ConstellationRefreshTarget(
            refreshInstant = refreshInstant,
            targetMidnight = targetMidnight,
            nextRefreshInstant = nextRefreshInstant
        )
    }

    private fun refreshInstantFor(date: LocalDate, observer: AstroObserver): Instant =
        sunriseProvider.sunriseOn(date, observer)
            ?: date.atTime(FALLBACK_REFRESH_TIME)
                .atZone(observer.zoneId)
                .toInstant()

    private companion object {
        private val FALLBACK_REFRESH_TIME = LocalTime.of(6, 0)
    }
}
