package com.derfence.astroface.wear.complication

import com.derfence.astroface.wear.dial.DialRenderer
import com.derfence.astroface.wear.dial.MoonSurfaceTexture
import com.derfence.astroface.wear.dial.StatusOverlayRenderer
import com.derfence.astroface.wear.status.DefaultWatchStatusSource
import com.derfence.astroface.wear.status.RequestStatusRepository
import java.time.Duration

class StatusOverlayDataSourceService : AstroFaceDialDataSourceService() {
    override val renderer: DialRenderer by lazy {
        StatusOverlayRenderer(
            statusSource = DefaultWatchStatusSource(),
            moonSurface = MoonSurfaceTexture.decode(resources)
        )
    }

    override val statusKey = RequestStatusRepository.DialKey.STATUS_OVERLAY
    override val timelinePlan = DialTimelinePlan.ByValidity(Duration.ofHours(48))
}
