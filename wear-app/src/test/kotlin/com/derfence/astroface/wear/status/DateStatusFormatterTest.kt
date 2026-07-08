package com.derfence.astroface.wear.status

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class DateStatusFormatterTest {
    @Test
    fun dateUsesFrenchCompactWatchFaceFormat() {
        val label = DateStatusFormatter()
            .labelFor(Instant.parse("2026-07-04T10:00:00Z"))

        assertEquals("sam. 04 juil.", label)
    }
}
