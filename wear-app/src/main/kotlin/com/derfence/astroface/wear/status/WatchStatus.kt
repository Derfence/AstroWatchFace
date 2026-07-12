package com.derfence.astroface.wear.status

import com.derfence.astroface.wear.astro.MoonPhaseSnapshot

data class WatchStatus(
    val dateLabel: String,
    val moonPhase: MoonPhaseSnapshot
)
