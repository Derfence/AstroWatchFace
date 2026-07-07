package com.derfence.astroface.wear.astro

import io.github.cosinekitty.astronomy.Observer
import io.github.cosinekitty.astronomy.Time
import java.time.Instant

internal fun AstroObserver.toAstronomyObserver(): Observer =
    Observer(latitude, longitude, elevationMeters)

internal fun Instant.toAstronomyTime(): Time =
    Time.fromMillisecondsSince1970(toEpochMilli())

internal fun Time.toInstant(): Instant =
    Instant.ofEpochMilli(toMillisecondsSince1970())
