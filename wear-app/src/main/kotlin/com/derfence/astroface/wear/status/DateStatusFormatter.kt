package com.derfence.astroface.wear.status

import com.derfence.astroface.wear.astro.AstroObserver
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class DateStatusFormatter(
    private val zoneId: ZoneId = AstroObserver.DEFAULT.zoneId,
    private val locale: Locale = Locale.FRANCE
) {
    private val formatter = DateTimeFormatter.ofPattern("EEE dd MMM", locale)

    fun labelFor(time: Instant): String =
        formatter.format(time.atZone(zoneId)).lowercase(locale)
}

