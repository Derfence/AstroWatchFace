package com.derfence.astroface.wear.complication

import com.derfence.astroface.wear.dial.Dial24hRenderer
import com.derfence.astroface.wear.dial.DialRenderer
import com.derfence.astroface.wear.status.RequestStatusRepository

class Dial24hDataSourceService : AstroFaceDialDataSourceService() {
    override val renderer: DialRenderer = Dial24hRenderer()
    override val statusKey = RequestStatusRepository.DialKey.DIAL_24H
}
