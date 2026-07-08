package com.derfence.astroface.wear.astro

import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class ConstellationRefreshPolicyTest {
    @Test
    fun afterSunriseTargetsFollowingNightAtMidnight() {
        val policy = ConstellationRefreshPolicy(
            MapSunriseProvider(
                "2026-07-08" to "2026-07-08T04:00:00Z",
                "2026-07-09" to "2026-07-09T04:01:00Z"
            )
        )

        val target = policy.targetFor(
            Instant.parse("2026-07-08T05:00:00Z"),
            AstroObserver.DEFAULT
        )

        assertEquals(Instant.parse("2026-07-08T04:00:00Z"), target.refreshInstant)
        assertEquals(Instant.parse("2026-07-08T22:00:00Z"), target.targetMidnight)
        assertEquals(Instant.parse("2026-07-09T04:01:00Z"), target.nextRefreshInstant)
    }

    @Test
    fun beforeSunriseKeepsPreviousRefreshAndCurrentNightMidnight() {
        val policy = ConstellationRefreshPolicy(
            MapSunriseProvider(
                "2026-07-07" to "2026-07-07T03:59:00Z",
                "2026-07-08" to "2026-07-08T04:00:00Z"
            )
        )

        val target = policy.targetFor(
            Instant.parse("2026-07-08T03:30:00Z"),
            AstroObserver.DEFAULT
        )

        assertEquals(Instant.parse("2026-07-07T03:59:00Z"), target.refreshInstant)
        assertEquals(Instant.parse("2026-07-07T22:00:00Z"), target.targetMidnight)
        assertEquals(Instant.parse("2026-07-08T04:00:00Z"), target.nextRefreshInstant)
    }

    @Test
    fun exactSunriseSwitchesToFollowingNight() {
        val policy = ConstellationRefreshPolicy(
            MapSunriseProvider(
                "2026-07-08" to "2026-07-08T04:00:00Z",
                "2026-07-09" to "2026-07-09T04:01:00Z"
            )
        )

        val target = policy.targetFor(
            Instant.parse("2026-07-08T04:00:00Z"),
            AstroObserver.DEFAULT
        )

        assertEquals(Instant.parse("2026-07-08T04:00:00Z"), target.refreshInstant)
        assertEquals(Instant.parse("2026-07-08T22:00:00Z"), target.targetMidnight)
        assertEquals(Instant.parse("2026-07-09T04:01:00Z"), target.nextRefreshInstant)
    }

    private class MapSunriseProvider(
        vararg entries: Pair<String, String>
    ) : SunriseProvider {
        private val sunrises = entries.associate {
            LocalDate.parse(it.first) to Instant.parse(it.second)
        }

        override fun sunriseOn(date: LocalDate, observer: AstroObserver): Instant? =
            sunrises[date]
    }
}
