package com.derfence.astroface.wear.complication

import com.derfence.astroface.wear.dial.CelestialHorizonOverlayRenderer
import com.derfence.astroface.wear.dial.DialRenderer
import com.derfence.astroface.wear.status.RequestStatusRepository
import java.time.Duration

class CelestialHorizonDataSourceService : AstroFaceDialDataSourceService() {
    override val renderer: DialRenderer = CelestialHorizonOverlayRenderer()
    override val statusKey = RequestStatusRepository.DialKey.CELESTIAL_HORIZON
    override val timelinePlan = DialTimelinePlan.ByValidity(Duration.ofHours(48))
}
