package com.derfence.astroface.wear.complication

import com.derfence.astroface.wear.dial.DialRenderer
import com.derfence.astroface.wear.dial.StatusOverlayRenderer
import com.derfence.astroface.wear.status.AndroidBatteryStatusSource
import com.derfence.astroface.wear.status.DefaultWatchStatusSource
import com.derfence.astroface.wear.status.RequestStatusRepository

class StatusOverlayDataSourceService : AstroFaceDialDataSourceService() {
    override val renderer: DialRenderer
        get() = StatusOverlayRenderer(
            statusSource = DefaultWatchStatusSource(
                batteryStatusSource = AndroidBatteryStatusSource(applicationContext)
            )
        )

    override val statusKey = RequestStatusRepository.DialKey.STATUS_OVERLAY
}
