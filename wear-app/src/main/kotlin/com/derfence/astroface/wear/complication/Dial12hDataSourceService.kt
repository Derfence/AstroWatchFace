package com.derfence.astroface.wear.complication

import com.derfence.astroface.wear.dial.Dial12hRenderer
import com.derfence.astroface.wear.dial.DialRenderer
import com.derfence.astroface.wear.status.RequestStatusRepository

class Dial12hDataSourceService : AstroFaceDialDataSourceService() {
    override val renderer: DialRenderer = Dial12hRenderer()
    override val statusKey = RequestStatusRepository.DialKey.DIAL_12H
}
