package com.derfence.astroface.wear.complication

import com.derfence.astroface.wear.dial.DialRenderer
import com.derfence.astroface.wear.dial.Hour24hHandRenderer
import com.derfence.astroface.wear.status.RequestStatusRepository

class Hour24hHandDataSourceService : AstroFaceDialDataSourceService() {
    override val renderer: DialRenderer = Hour24hHandRenderer()
    override val statusKey = RequestStatusRepository.DialKey.HOUR_24H_HAND
}
