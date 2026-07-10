package com.derfence.astroface.wear.complication

import com.derfence.astroface.wear.dial.DialRenderer
import com.derfence.astroface.wear.dial.ModeOverlayRenderer
import com.derfence.astroface.wear.display.DisplayModeRepository
import com.derfence.astroface.wear.status.RequestStatusRepository

class ModeOverlayDataSourceService : AstroFaceDialDataSourceService() {
    override val renderer: DialRenderer
        get() = ModeOverlayRenderer(
            mode = DisplayModeRepository.read(applicationContext)
        )

    override val statusKey = RequestStatusRepository.DialKey.MODE_OVERLAY
}
