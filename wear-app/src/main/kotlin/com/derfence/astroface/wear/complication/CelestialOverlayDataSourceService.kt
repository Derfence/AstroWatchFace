package com.derfence.astroface.wear.complication

import com.derfence.astroface.wear.dial.CelestialOverlayRenderer
import com.derfence.astroface.wear.dial.DialRenderer
import com.derfence.astroface.wear.status.RequestStatusRepository

class CelestialOverlayDataSourceService : AstroFaceDialDataSourceService() {
    override val renderer: DialRenderer = CelestialOverlayRenderer()
    override val statusKey = RequestStatusRepository.DialKey.CELESTIAL_OVERLAY
}
