package com.derfence.astroface.wear.complication

import com.derfence.astroface.wear.dial.DialRenderer
import com.derfence.astroface.wear.dial.ModeOverlayRenderer
import com.derfence.astroface.wear.display.DisplayMode
import com.derfence.astroface.wear.display.DisplayModeRepository
import com.derfence.astroface.wear.status.RequestStatusRepository

class ModeOverlayDataSourceService : AstroFaceDialDataSourceService() {
    private val displayMode: DisplayMode
        get() = DisplayModeRepository.read(applicationContext)

    override val renderer: DialRenderer
        get() = ModeOverlayRenderer(
            mode = displayMode
        )

    override val statusKey = RequestStatusRepository.DialKey.MODE_OVERLAY

    override val timelineSchedule: DialTimelineSchedule?
        get() = when (displayMode) {
            DisplayMode.FULL_DIAL -> null
            DisplayMode.CONSTELLATIONS_NIGHT,
            DisplayMode.SOLAR_SYSTEM -> DialTimelineSchedule.PassageMode
        }
}
