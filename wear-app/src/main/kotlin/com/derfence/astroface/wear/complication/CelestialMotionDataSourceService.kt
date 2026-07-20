package com.derfence.astroface.wear.complication

import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.NoDataComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import com.derfence.astroface.wear.status.RequestStatusRepository
import java.time.Instant

abstract class CelestialMotionDataSourceService : ComplicationDataSourceService() {
    protected abstract val group: CelestialMotionGroup

    override fun onComplicationRequest(
        request: ComplicationRequest,
        listener: ComplicationRequestListener
    ) {
        RequestStatusRepository.markRequest(
            applicationContext,
            RequestStatusRepository.DialKey.CELESTIAL_MOTION,
            request
        )
        if (request.complicationType != ComplicationType.RANGED_VALUE) {
            listener.onComplicationData(NoDataComplicationData())
            return
        }
        listener.onComplicationDataTimeline(
            CelestialMotionComplicationDataFactory.createTimeline(group, Instant.now())
        )
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData =
        if (type == ComplicationType.RANGED_VALUE) {
            CelestialMotionComplicationDataFactory.createCurrent(group, Instant.now())
        } else {
            NoDataComplicationData()
        }
}

class CelestialInnerMotionDataSourceService : CelestialMotionDataSourceService() {
    override val group = CelestialMotionGroup.INNER
}

class CelestialMiddleMotionDataSourceService : CelestialMotionDataSourceService() {
    override val group = CelestialMotionGroup.MIDDLE
}

class CelestialOuterMotionDataSourceService : CelestialMotionDataSourceService() {
    override val group = CelestialMotionGroup.OUTER
}
