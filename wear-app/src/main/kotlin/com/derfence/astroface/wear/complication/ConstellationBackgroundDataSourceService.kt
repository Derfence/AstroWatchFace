package com.derfence.astroface.wear.complication

import com.derfence.astroface.wear.dial.ConstellationLayerRenderer
import com.derfence.astroface.wear.dial.ConstellationLayerStyle
import com.derfence.astroface.wear.dial.DialRenderer
import com.derfence.astroface.wear.status.RequestStatusRepository
import java.time.Duration

class ConstellationBackgroundDataSourceService : AstroFaceDialDataSourceService() {
    override val renderer: DialRenderer = ConstellationLayerRenderer(ConstellationLayerStyle.MAIN)
    override val statusKey = RequestStatusRepository.DialKey.CONSTELLATION_BACKGROUND
    override val timelinePlan = DialTimelinePlan.ByValidity(Duration.ofHours(48))
}
