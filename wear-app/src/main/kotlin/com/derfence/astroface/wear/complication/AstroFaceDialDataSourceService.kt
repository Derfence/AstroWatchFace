package com.derfence.astroface.wear.complication

import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.NoDataComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import com.derfence.astroface.wear.dial.DialRenderer
import com.derfence.astroface.wear.status.RequestStatusRepository
import java.time.Instant

abstract class AstroFaceDialDataSourceService : ComplicationDataSourceService() {
    protected abstract val renderer: DialRenderer
    protected abstract val statusKey: RequestStatusRepository.DialKey
    protected open val timelinePlan: DialTimelinePlan? =
        DialTimelinePlan.Fixed(DialTimelineSchedule.WatchMode)
    protected open val imageEnabled: Boolean = true

    override fun onComplicationRequest(
        request: ComplicationRequest,
        listener: ComplicationRequestListener
    ) {
        RequestStatusRepository.markRequest(applicationContext, statusKey, request)

        if (request.complicationType != ComplicationType.PHOTO_IMAGE || !imageEnabled) {
            listener.onComplicationData(NoDataComplicationData())
            return
        }

        val start = Instant.now()
        val plan = timelinePlan
        if (plan != null) {
            listener.onComplicationDataTimeline(
                DialComplicationDataFactory.createTimeline(renderer, start, plan)
            )
        } else {
            listener.onComplicationData(DialComplicationDataFactory.create(renderer, start))
        }
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? =
        if (type == ComplicationType.PHOTO_IMAGE && imageEnabled) {
            DialComplicationDataFactory.create(renderer, Instant.now())
        } else {
            NoDataComplicationData()
        }
}
